package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash, Terminated}
import it.unibo.intelliserra.common.communication.Protocol._


/**
 * This is the Zone Manager actor which is in charge to create new zone actors when
 * a client needs them, it keeps link between zone identifier (given by the client)
 * and the actor ref. It also be able to delete zones and check for zone existence
 */
private[zone] class ZoneManagerActor extends Actor with ActorLogging with Stash {

  var zones: Map[String, ActorRef] = Map()
  implicit val system: ActorSystem = context.system

  private def idle : Receive = {
    case CreateZone(identifier) if zones.contains(identifier) => sender() ! ZoneCreationError
    case CreateZone(identifier) =>
      val zoneActorRef = ZoneActor(identifier)
      context watch zoneActorRef
      zones = zones + (identifier -> zoneActorRef)
      sender() ! ZoneCreated
    case ZoneExists(identifier) if zones.contains(identifier) => sender() ! Zone(zones(identifier))
    case ZoneExists(_) => sender() ! NoZone
    case RemoveZone(identifier) if zones.contains(identifier) =>
      zones(identifier) ! DestroyYourself
      context.become(waitForZoneDead(sender(), identifier))
    case RemoveZone(_) => sender() ! NoZone
    case GetZones => sender() ! Zones(zones.keySet.toList)
  }

  private def waitForZoneDead(replyTo: ActorRef, identifier: String): Receive = {
    case Terminated(_) =>
      //TODO devo controllare se la zoneRef che mi arriva in terminated è una che è contenuta nella mia mappa?
      zones = zones - identifier
      context.become(idle)
      unstashAll()
      replyTo ! ZoneRemoved
    case _ => stash()
  }


  override def receive: Receive = idle
}

object ZoneManagerActor {
  val name = "ZoneManager"
  def apply()(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf (Props[ZoneManagerActor], name)
}