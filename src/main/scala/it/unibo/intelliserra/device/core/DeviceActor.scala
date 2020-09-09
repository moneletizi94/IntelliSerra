package it.unibo.intelliserra.device.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import it.unibo.intelliserra.common.akka.actor.DefaultExecutionContext
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom}
import it.unibo.intelliserra.core.Device

trait DeviceActor extends Actor with ActorLogging {

  protected def device: Device

  override def preStart(): Unit = {
    super.preStart()
    device.onInit()
  }

  def zoneManagement: Receive = {
    case AssociateTo(zoneRef, zoneName) =>

    case DissociateFrom(zoneRef, zoneName) =>

  }


  protected val fallback: Receive = {
    case msg@_ => log.debug(s"unknown message: $msg")
  }
}