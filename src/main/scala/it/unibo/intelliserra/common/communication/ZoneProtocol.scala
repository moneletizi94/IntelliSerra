package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef

trait ZoneProtocol {

  case class CreateZone(identifier: String)
  //An entity could ask whether a zone exists (used also for testing createZone and removeZone)
  case class ZoneExists(identifier: String)

  case object ZoneCreationOk
  case object ZoneCreationError

  case class Zone(zoneRef: ActorRef)
  case object NoZone

}
