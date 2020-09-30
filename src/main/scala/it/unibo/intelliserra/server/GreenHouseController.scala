package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import it.unibo.intelliserra.common.akka.actor.{DefaultExecutionContext, DefaultTimeout}
import it.unibo.intelliserra.common.communication.Messages
import it.unibo.intelliserra.common.communication.Messages.ZoneManagerRequest
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.GreenHouseController.ResponseMap
import it.unibo.intelliserra.server.entityManager.DeviceChannel

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * This is the Actor Controller who is in charge of receiving client messages and sending them to other entities based on the request.
 * He also knows the references to the main entities such as zoneManagerActor and entityManagerActor.
 *
 * @param zoneManagerActor   , actorRef
 * @param entityManagerActor , actorRef
 */
private[server] class GreenHouseController(zoneManagerActor: ActorRef,
                                           entityManagerActor: ActorRef,
                                           ruleEngineServiceActor: ActorRef)
  extends Actor
    with DefaultExecutionContext
    with DefaultTimeout {

  private implicit val system: ActorSystem = context.system

  /* --- ON RECEIVE ACTIONS --- */
  override def receive: Receive = {

    case CreateZone(zoneName) => onCreateZone(zoneName)

    case DeleteZone(zoneName) => onDeleteZone(zoneName)

    case GetZones() => onGetZones()

    case GetState(zoneName) => onGetState(zoneName)

    case AssignEntity(zoneName, entityId) => onAssignEntity(zoneName, entityId)

    case DissociateEntity(entityId) => onDissociateEntity(entityId)

    case RemoveEntity(entityId) => onRemoveEntity(entityId)

    case GetRules => onGetRules()

    case EnableRule(ruleID) => onEnableRule(ruleID)

    case DisableRule(ruleID) => onDisableRule(ruleID)
  }

  private def onCreateZone(zoneName: String): Unit = {
    sendResponseWithFallback(zoneManagerActor ? Messages.CreateZone(zoneName), sender()) {
      case Success(Messages.ZoneCreated) => ServiceResponse(Created)
      case Success(Messages.ZoneAlreadyExists) => ServiceResponse(Conflict, "Zone already exists")
    }
  }

  private def onDeleteZone(zoneName: String): Unit = {
    sendResponseWithFallback(zoneManagerActor ? Messages.RemoveZone(zoneName), sender()) {
      case Success(Messages.ZoneRemoved) => ServiceResponse(Deleted)
      case Success(Messages.ZoneNotFound) => ServiceResponse(NotFound, "Zone not found")
    }
  }

  private def onGetZones(): Unit = {
    sendResponseWithFallback(zoneManagerActor ? Messages.GetZones, sender()) {
      case Success(Messages.ZonesResult(zones)) => ServiceResponse(Ok, zones)
    }
  }

  private def onGetState(zoneName: String): Unit = {
    sendResponseWithFallback(zoneManagerActor ? Messages.GetZoneState(zoneName), sender()) {
      case Success(Messages.MyState(state)) => ServiceResponse(Ok, state)
      case Success(Messages.ZoneNotFound) => ServiceResponse(NotFound, "Zone not found")
    }
  }

  private def onAssignEntity(zoneName: String, entityId: String): Unit = {
    val association = sendToZoneManager(entityId, channel => Messages.AssignEntityToZone(zoneName,channel))

    sendResponseWithFallback(association, sender()) {
      case Success(Messages.EntityNotFound) => ServiceResponse(NotFound, "Entity not found")
      case Success(Messages.ZoneNotFound) => ServiceResponse(NotFound, "Zone not found")
      case Success(Messages.AlreadyAssigned(zone)) => ServiceResponse(Conflict, "Entity already assigned to " + zone)
      case Success(Messages.AssignOk) => ServiceResponse(Ok)
      case Success(Messages.AssignError(error)) => ServiceResponse(Error, error)
    }
  }

  private def onDissociateEntity(entityId: String): Unit = {
    val association = sendToZoneManager(entityId, Messages.DissociateEntityFromZone)

    sendResponseWithFallback(association, sender()) {
      case Success(Messages.DissociateOk) => ServiceResponse(Ok)
      case Success(Messages.AlreadyDissociated) => ServiceResponse(Error, "Entity already dissociated")
      case Success(Messages.EntityNotFound) => ServiceResponse(NotFound, "Entity not found")
    }
  }

  private def sendToZoneManager(entityId: String, request: DeviceChannel => ZoneManagerRequest): Future[Any] = {
    entityManagerActor ? Messages.GetEntity(entityId) flatMap {
      case Messages.EntityResult(entity) =>
        zoneManagerActor ? request(entity)
      case msg => Future.successful(msg)
    }
  }

  private def onRemoveEntity(entityId: String): Unit = {
    sendResponseWithFallback(entityManagerActor ? Messages.RemoveEntity(entityId), sender()) {
      case Success(Messages.EntityNotFound) => ServiceResponse(NotFound, "Entity not found")
      case Success(Messages.EntityRemoved) => ServiceResponse(Deleted)
    }
  }

  private def onGetRules(): Unit = {
    sendResponseWithFallback(ruleEngineServiceActor ? Messages.GetRules, sender()) {
      case Success(Messages.Rules(rules)) => ServiceResponse(Ok, rules)
    }
  }

  private def onEnableRule(ruleID: String): Unit = {
    sendResponseWithFallback(ruleEngineServiceActor ? Messages.EnableRule(ruleID), sender()) {
      case Success(Messages.EnableOk) => ServiceResponse(Ok, "Rule enabled")
      case Success(Messages.Error) => ServiceResponse(Error, "not possible")
    }
  }

  private def onDisableRule(ruleID: String): Unit = {
    sendResponseWithFallback(ruleEngineServiceActor ? Messages.DisableRule(ruleID), sender()) {
      case Success(Messages.DisableOk) => ServiceResponse(Ok, "Rule disabled")
      case Success(Messages.Error) => ServiceResponse(Error, "not possible")
    }
  }

  private def sendResponseWithFallback[T](request: Future[T], replyTo: ActorRef)(mapSend: ResponseMap[T]): Unit = {
    request onComplete {
      sendResponseWithFallback(replyTo)(mapSend)
    }
  }

  private def sendResponse[T](replyTo: ActorRef)(mapSend: ResponseMap[T]): Try[T] => Unit = replyTo ! mapSend(_)

  private def sendResponseWithFallback[T](replyTo: ActorRef)(mapSend: ResponseMap[T]): Try[T] => Unit = sendResponse(replyTo)(mapSend orElse fallback)

  private def fallback[T]: ResponseMap[T] = {
    case Failure(exception) => ServiceResponse(Error, exception.toString)
    case _ => ServiceResponse(Error, "Internal Error")
  }
}

/** Factory for [[it.unibo.intelliserra.server.GreenHouseController]] instances. */
object GreenHouseController {
  type ResponseMap[T] = PartialFunction[Try[T], ServiceResponse]

  val name = "GreenHouseController"

  /**
   * Creates a GreenHouseController actor with a given the actorRef from the other actors.
   *
   * @param zoneManagerActor       actorRef representing a zoneManager actor,
   * @param entityManagerActor     actorRef representing a entityManager actor,
   * @param ruleEngineServiceActor actorRef representing a ruleEngineService actor,
   * @param actorSystem            represent the actorSystem,
   * @return actorRef representing an actor, which is a new GreenHouseController
   *         that contains the actorRef of the other actors and the specified name.
   */
  def apply(zoneManagerActor: ActorRef, entityManagerActor: ActorRef, ruleEngineServiceActor: ActorRef)(implicit actorSystem: ActorSystem): ActorRef =
    actorSystem actorOf(Props(new GreenHouseController(zoneManagerActor, entityManagerActor, ruleEngineServiceActor)), name)
}