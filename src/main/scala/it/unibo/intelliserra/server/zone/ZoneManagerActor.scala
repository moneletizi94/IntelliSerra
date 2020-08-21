package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.server.zone.ZoneManagerActor._


/**
 * This is the Zone Manager actor which is in charge to create new zone actors when
 * a client needs them, it keeps link between zone identifier (given by the client)
 * and the actor ref. It also be able to delete zones and check for zone existence
 */
private[zone] class ZoneManagerActor extends Actor with ActorLogging {

  var zones: Map[String, ActorRef] = Map()
  implicit val system: ActorSystem = context.system

  override def receive: Receive = {
    case CreateZone(identifier) if zones.contains(identifier) => sender() ! ZoneCreationError
    case CreateZone(identifier) => val zoneActorRef: ActorRef = ZoneActor(identifier)
      zones = zones + (identifier -> zoneActorRef)
      sender() ! ZoneCreationOk
    case ZoneExists(identifier) if zones.contains(identifier) => sender() ! Zone(zones(identifier))
    case ZoneExists(_) => sender() ! NoZone
  }
}

object ZoneManagerActor {
  val name = "ZoneManager"
  def apply()(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf (Props[ZoneManagerActor], name)

  //A client ask for a new Zone
  case class CreateZone(identifier: String)
  //An entity could ask whether a zone exists (used also for testing createZone and removeZone)
  case class ZoneExists(identifier: String)

  case object ZoneCreationOk
  case object ZoneCreationError

  case class Zone(zoneRef: ActorRef)
  case object NoZone
}