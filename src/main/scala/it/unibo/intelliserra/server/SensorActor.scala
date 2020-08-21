package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.sensor.Sensor

class SensorActor(private val sensorInfo : Sensor) extends Actor{
  override def receive: Receive = {case _ => }

}

object SensorActor{

  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new SensorActor(sensor)), name = sensor.identifier)
  }
}
