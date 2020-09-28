package it.unibo.intelliserra.device.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom}

trait DeviceActor extends Actor with ActorLogging {

  def handleZoneManagement(actualZone: Option[ActorRef]): Receive = {
    case AssociateTo(zoneRef, zoneName) if actualZone.fold(true)(_ != zoneRef) =>
      sender() ! Ack
      onAssociated(zoneRef, zoneName)
      context.become(handleZoneManagement(Option(zoneRef)) orElse associatedBehaviour(zoneRef, zoneName) orElse fallback)
    case DissociateFrom(zoneRef, zoneName) if actualZone.fold(false)(_ == zoneRef) =>
      onDissociate(zoneRef, zoneName)
      context.become(handleZoneManagement(None) orElse dissociatedBehaviour(zoneRef, zoneName) orElse fallback)
  }

  protected def onAssociated(zoneRef: ActorRef, zoneName: String): Unit
  protected def onDissociate(zoneRef: ActorRef, zoneName: String): Unit

  protected def associatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive
  protected def dissociatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive

  private def fallback: Receive = {
    case msg@_ => log.info(s"Unknown message: $msg")
  }
}
