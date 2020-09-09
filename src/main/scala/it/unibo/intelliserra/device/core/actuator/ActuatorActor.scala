package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.actuator._
import it.unibo.intelliserra.device.core.DeviceActor

case class CompleteAction(action: Action, whenComplete: Action => Unit)

class ActuatorActor(override val device: Actuator) extends DeviceActor {


  override def receive: Receive = zoneManagement orElse fallback
}

object ActuatorActor {

  case class DoActions(actions: Set[Action])
  case class ActuatorStateChanged(operationalState: OperationalState)

  private case class OnCompleteAction(action: Action)

  def apply(actuator: Actuator)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(actuator)
  def props(actuator: Actuator): Props = Props(new ActuatorActor(actuator))
}