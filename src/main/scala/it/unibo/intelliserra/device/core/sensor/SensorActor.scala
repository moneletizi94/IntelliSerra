package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.core.DeviceActor

class SensorActor(override val device: Sensor) extends DeviceActor {

  override def receive: Receive = zoneManagement orElse fallback

  override protected def associateBehaviour(zoneRef: ActorRef): Receive = {
    case _ => {}
  }

  override protected def dissociateBehaviour(zoneRef: ActorRef): Receive = {
    case _ => {}
  }
}

object SensorActor {
  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(sensor)
  def props(sensor: Sensor): Props = Props(new SensorActor(sensor))
}
