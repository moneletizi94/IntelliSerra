package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.server.core.RegisteredEntity

object Protocol {

  sealed trait JoinRequest
  case class JoinSensor(identifier: String, sensingCapability: SensingCapability, sensorRef: ActorRef) extends JoinRequest
  case class JoinActuator(identifier: String, actingCapability: ActingCapability, actuatorRef : ActorRef) extends JoinRequest

  sealed trait JoinResponse
  case object JoinOK extends JoinResponse
  case class JoinError(error:String) extends JoinResponse

  /* --- From GH to ZoneManager --- */
  //A client asks for a new Zone
  final case class CreateZone(identifier: String)
  //An entity could ask whether a zone exists (used also for testing createZone and removeZone)
  final case class ZoneExists(identifier: String)
  //A client asks to remove a zone, the corresponding actor will be stopped
  case class RemoveZone(identifier: String)
  //GH asks for all the zones in the ZoneManager
  case object getZones

  /* --- From ZoneManager to GH --- */
  case object ZoneCreated
  case object ZoneCreationError
  //Used to Answer to getZones
  case class Zones(zones: List[String])

  //Used to answer to ZoneExists
  case class Zone(zoneRef: ActorRef)
  //Used to answer to both ZoneExists and RemoveZone (when the specified zone doesn't exists)
  case object NoZone

  //Used to answer to RemoveZone
  case object ZoneRemoved

  /* --- From ZoneManager to Zone --- */
  //Used when a client wants to remove a zone,
  // the zone will not be reachable anymore. It should inform its sensor/actuator
  case object DestroyYourself

  /* --- From Zone to Sensor/ Actuator --- */
  case class DissociateFromMe(zoneRef: ActorRef)

  /* --- From Zone to Sensor/ Actuator --- */
  case class AssociateToMe(zoneRef: ActorRef)

  /* --- From GH to Zone --- */
  case class AssignSensor(actorRef: ActorRef, registeredEntity: RegisteredEntity)
  case class DeAssignSensor(actorRef: ActorRef)

  /* --- From GH to Zone --- */
  case class AssignActuator(actorRef: ActorRef, registeredEntity: RegisteredEntity)
  case class DeAssignActuator(actorRef: ActorRef)
  case class IsEntityAssociated(entityRef : ActorRef)
  /** -- From Zone to GH */
  trait IsAssociatedResponse
  case class IsAssociated(actorRef: ActorRef) extends IsAssociatedResponse
  case object IsNotAssociated extends IsAssociatedResponse
  /** -- From Zone to GH */
  case object AssignOk

}
