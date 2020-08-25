package it.unibo.intelliserra.client.core

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import it.unibo.intelliserra.common.akka.actor.{DefaultExecutionContext, DefaultTimeout}
import it.unibo.intelliserra.common.communication.Protocol._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

private[core] object Client {

  /**
   * Create a client using akka actor
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

    private def handleRequest: Receive = {
      case CreateZone(zone) =>
        doRequest(CreateZone(zone)) {
          case ZoneCreated => Success(zone)
          case ZoneCreationError => Failure(new IllegalStateException("fail during zone creation"))
        }

      case RemoveZone(zone) =>
        doRequest(RemoveZone(zone)) {
          case ZoneRemoved => Success(zone)
          case NoZone => Failure(new IllegalArgumentException("zone not found"))
        }

      case GetZones =>
        doRequest(GetZones) { case Zones(zones) => Success(zones) }

      case msg => log.debug(s"ignored unknown request $msg")
    }


    private def doRequest(msg: => Any)(responseTransform: Any => Try[Any]): Unit = {
      serverActor ? msg flatMap { msg => Future.fromTry(responseTransform(msg)) } pipeTo sender()
    }

    override def receive: Receive = handleRequest
  }
}