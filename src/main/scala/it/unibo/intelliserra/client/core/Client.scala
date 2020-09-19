package it.unibo.intelliserra.client.core

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import it.unibo.intelliserra.common.akka.actor.{DefaultExecutionContext, DefaultTimeout}
import it.unibo.intelliserra.common.communication.Protocol.{ClientRequest, DisableRule, EnableRule, GetRules, ServiceResponse, _}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

private[core] object Client {

  type ServiceResponseMap[T] = PartialFunction[ServiceResponse, Try[T]]

  /**
   * Create a client using akka actor
   *
   * @param serverUri   the uri of server actor
   * @param actorSystem the actorSystem to be used for create the client actor
   * @return a new instance of client
   */
  def apply(serverUri: String)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf Props(new ClientImpl(serverUri))

  private[core] class ClientImpl(serverUri: String) extends Actor
    with DefaultTimeout
    with DefaultExecutionContext
    with ActorLogging {

    private val serverActor = context actorSelection serverUri

    private def handleZoneRequests: Receive = {

      case CreateZone(zone) => makeRequestWithFallback(CreateZone(zone)) {
        case ServiceResponse(Created, _) => Success(zone)
      }

      case DeleteZone(zone) => makeRequestWithFallback(DeleteZone(zone)) {
        case ServiceResponse(Deleted, _) => Success(zone)
      }

      case GetZones() => makeRequestWithFallback(GetZones()) {
        case ServiceResponse(Ok, zones) => Success(zones.asInstanceOf[List[String]])
      }

      case GetState(zone) => makeRequest(GetState(zone)) {
        case ServiceResponse(Ok, state) => Success(state)
      }
    }

    private def handleEntitiesRequests: Receive = {

      case AssignEntity(zoneID, entityID) => makeRequestWithFallback(AssignEntity(zoneID, entityID)) {
        case ServiceResponse(Ok,_) => Success(zoneID)
      }

      case DissociateEntity(entityID) => makeRequestWithFallback(DissociateEntity(entityID)) {
        case ServiceResponse(Ok, _) => Success(entityID)
      }

      case RemoveEntity(entityID) => makeRequestWithFallback(RemoveEntity(entityID)) {
        case ServiceResponse(Deleted, _) => Success(entityID)
      }
    }

    private def handleRulesRequests: Receive = {

      case GetRules => makeRequest(GetRules) {
        case ServiceResponse(Ok, rules) => Success(rules)
      }

      case EnableRule(ruleID) => ruleMode(EnableRule(ruleID))

      case DisableRule(ruleID) => ruleMode(DisableRule(ruleID))
    }

    private def ruleMode(request: ClientRequest): Unit = {
      makeRequestWithFallback(request) {
        case ServiceResponse(Ok, str) => Success(str)
      }
    }

    private def makeRequestWithFallback[T](request: => ClientRequest)(function: ServiceResponseMap[T]): Future[T] = {
      makeRequest(request)(function orElse fallback)
    }

    private def makeRequest[T](request: => ClientRequest)(function: ServiceResponseMap[T]): Future[T] = {
      (serverActor ? request).mapTo[ServiceResponse] flatMap { response => Future.fromTry(function(response)) } pipeTo sender()
    }

    private def fallback[T]: ServiceResponseMap[T] = {
      case ServiceResponse(_, errMsg) => Failure(new IllegalArgumentException(errMsg.toString))
      case _ => Failure(new Exception())
    }

    override def receive: Receive =
      handleZoneRequests orElse
      handleEntitiesRequests orElse
        handleRulesRequests orElse{
        case msg@_ => log.warning(s"Unknown message: $msg")
      }
  }
}