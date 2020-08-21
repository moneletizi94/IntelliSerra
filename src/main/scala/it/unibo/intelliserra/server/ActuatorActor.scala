package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.actuator.Actuator

class ActuatorActor(private val actuatorInfo : Actuator) extends Actor{
  override def receive: Receive = {case _ => }
}

object ActuatorActor{

  def apply(actuator: Actuator)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new ActuatorActor(actuator)), name = actuator.identifier)
  }
}
