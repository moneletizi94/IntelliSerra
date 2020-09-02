package it.unibo.intelliserra.device.core

import akka.actor.{Actor, ActorLogging, ActorRef}
import it.unibo.intelliserra.common.akka.actor.DefaultExecutionContext
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom}

private[core] trait EntityActor extends Actor
  with ActorLogging
  with DefaultExecutionContext {

  //noinspection ActorMutableStateInspection
  private var _zone: Option[ActorRef] = None
  protected[core] def zone: Option[ActorRef] = _zone

  def zoneManagement: Receive = {
    case AssociateTo(zoneRef,_) => _zone = Option(zoneRef); sender() ! Ack
    case DissociateFrom(zoneRef, _) => _zone = zone.filterNot(_ == zoneRef)
  }

  def sendToZone(msg: Any): Unit = {
    _zone.foreach(_ ! msg)
  }

  def fallback: Receive = {
    case msg@_ => log.debug(s"unknown message: $msg")
  }
}