package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.{ActuatorStateChanged, DoActions}
import it.unibo.intelliserra.core.action._
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.actuator.ActuatorActor.OnCompleteAction

class ActuatorActor(private val actuator: Actuator) extends DeviceActor with Timers {

  override protected def onAssociated(zoneRef: ActorRef, zoneName: String): Unit = {}
  override protected def onDissociate(zoneRef: ActorRef, zoneName: String): Unit = {}

  override protected def associatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive = {
    case DoActions(actions) =>
      for {
        action <- actions
        operation <- actuator.handleAction(action)
      } schedule(action, operation)
      zoneRef ! ActuatorStateChanged(actuator.state)

    case OnCompleteAction(action) =>
      zoneRef ! ActuatorStateChanged(actuator.handleCompletedAction(action))
  }

  override protected def dissociatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive = {
    case OnCompleteAction(action) => actuator.handleCompletedAction(action)
  }

  override def receive: Receive = handleZoneManagement(None)

  private def schedule(action: Action, timedTask: Operation): Unit = {
    timers.startSingleTimer(action, OnCompleteAction(action), timedTask.delay)
  }
}

object ActuatorActor {
  private[actuator] case class OnCompleteAction(action: Action)

  def apply(actuator: Actuator)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(actuator)
  def props(actuator: Actuator): Props = Props(new ActuatorActor(actuator))
}