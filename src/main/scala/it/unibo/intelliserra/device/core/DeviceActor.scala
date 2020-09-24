package it.unibo.intelliserra.device.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom}
import it.unibo.intelliserra.core.entity.Device

trait DeviceActor extends Actor with ActorLogging {

  protected def device: DeviceCallback

  override def preStart(): Unit = {
    super.preStart()
    device.onInit()
  }

  def zoneManagement: Receive = {
    case AssociateTo(zoneRef, zoneName) =>
      context.become(associateBehaviour(zoneRef) orElse zoneManagement)
      sender() ! Ack
      device.onAssociateZone(zoneName)

    case DissociateFrom(zoneRef, zoneName) =>
      device.onDissociateZone(zoneName)
      context.become(dissociateBehaviour(zoneRef) orElse zoneManagement)
  }

  protected def associateBehaviour(zoneRef: ActorRef): Receive
  protected def dissociateBehaviour(zoneRef: ActorRef): Receive

  protected val fallback: Receive = {
    case msg@_ => log.debug(s"unknown message: $msg")
  }
}