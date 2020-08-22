package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}

object Protocol {

  sealed trait JoinRequest
  case class JoinSensor(identifier: String, sensingCapability: SensingCapability, sensorRef: ActorRef) extends JoinRequest
  case class JoinActuator(identifier: String, actingCapability: ActingCapability, actuatorRef : ActorRef) extends JoinRequest

  sealed trait JoinResult
  case object JoinOK extends JoinResult
  case class JoinError(error:String) extends JoinResult

  /* --- From GH to ZoneManager --- */
  //A client ask for a new Zone
  final case class CreateZone(identifier: String)
  //An entity could ask whether a zone exists (used also for testing createZone and removeZone)
  final case class ZoneExists(identifier: String)
  //A client ask to remove a zone, the corresponding actor will be stopped
  case class RemoveZone(identifier: String)

  /* --- From ZoneManager to GH --- */
  case object ZoneCreated
  case object ZoneCreationError

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
}
