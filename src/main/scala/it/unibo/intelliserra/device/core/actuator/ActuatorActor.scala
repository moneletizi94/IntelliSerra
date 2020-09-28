package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.{ActuatorStateChanged, DoActions}
import it.unibo.intelliserra.core.action._
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.actuator.ActuatorActor.OnOperationCompleted

object ActuatorActor {
  // self send message when an operation is completed
  private[device] case class OnOperationCompleted(action: Action)

  /**
   * Create a ActuatorActor from Actuator.
   * The actor act as controller of actuator.
   * @param actuator      the actuator to be controlled by
   * @param actorSystem   the actor system for create the actor
   * @return  an actor ref
   */
  def apply(actuator: Actuator)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(actuator)

  /**
   * Create a props configuration for instantiate an ActuatorActor.
   * The actor act as controller of actuator.
   * @param actuator  the actuator to be controlled by.
   * @return the configuration object for instantiate an actor
   */
  def props(actuator: Actuator): Props = Props(new ActuatorActor(actuator))
}

private[device] class ActuatorActor(private val actuator: Actuator) extends DeviceActor with Timers {

  override protected def onAssociated(zoneRef: ActorRef, zoneName: String): Unit = {}
  override protected def onDissociate(zoneRef: ActorRef, zoneName: String): Unit = {}

  override protected def associatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive = {
    case DoActions(actions) =>
      for {
        action <- actions
        operation <- actuator.handleAction(action)
      } schedule(action, operation)
      zoneRef ! ActuatorStateChanged(actuator.state)

    case OnOperationCompleted(action) =>
      zoneRef ! ActuatorStateChanged(actuator.handleCompletedAction(action))
  }

  override protected def dissociatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive = {
    case OnOperationCompleted(action) => actuator.handleCompletedAction(action)
  }

  override def receive: Receive = zoneManagementBehaviour(None)

  private def schedule(action: Action, operation: Operation): Unit = {
    timers.startSingleTimer(action, OnOperationCompleted(action), operation.timeout)
  }
}