package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.device.core.EntityActor

class ActuatorActor(private val actuator : Actuator) extends EntityActor {
  override def receive: Receive = zoneManagement orElse fallback
}

object ActuatorActor {
  def apply(actuator: Actuator)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new ActuatorActor(actuator)), name = actuator.identifier)
  }
}
