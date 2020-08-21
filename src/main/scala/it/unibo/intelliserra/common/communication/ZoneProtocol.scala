package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef

/**
 * This is the Zone protocol. There are messages from/to Zone and
 * Zone Manager actors
 */
trait ZoneProtocol {

  /* --- From GH to ZoneManager --- */
  //A client ask for a new Zone
  case class CreateZone(identifier: String)
  //An entity could ask whether a zone exists (used also for testing createZone and removeZone)
  case class ZoneExists(identifier: String)
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
