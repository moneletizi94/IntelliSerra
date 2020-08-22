package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Protocol._


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
    case CreateZone(identifier) =>
      val zoneActorRef = ZoneActor(identifier)
      zones = zones + (identifier -> zoneActorRef)
      sender() ! ZoneCreated
    case ZoneExists(identifier) if zones.contains(identifier) => sender() ! Zone(zones(identifier))
    case ZoneExists(_) => sender() ! NoZone
    case RemoveZone(identifier) if zones.contains(identifier) =>
      zones(identifier) ! DestroyYourself
      zones = zones - identifier
      sender() ! ZoneRemoved
    case RemoveZone(_) => sender() ! NoZone
  }
}

object ZoneManagerActor {
  val name = "ZoneManager"
  def apply()(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf (Props[ZoneManagerActor], name)
}