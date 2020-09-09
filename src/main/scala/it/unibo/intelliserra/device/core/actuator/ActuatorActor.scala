package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, Props, Timers}
import akka.pattern.pipe
import it.unibo.intelliserra.common.communication.Messages.DoActions
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.actuator._
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.actuator.ActuatorActor.{ActuatorStateChanged, OnCompleteAction}

import scala.concurrent.ExecutionContextExecutor

class ActuatorActor(override val device: Actuator) extends DeviceActor with Timers {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  private var operationalState: OperationalState = OperationalState()

  override def receive: Receive = zoneManagement orElse fallback

  override protected def associateBehaviour(zoneRef: ActorRef): Receive = {
    case DoActions(actions) =>
      val actionAllowed = actions.filter(action => device.capability.actions.contains(action) && !operationalState.isDoing(action))
      operationalState = dispatchActionsIfDefined(actionAllowed, operationalState, device.actionHandler)
      zoneRef ! ActuatorStateChanged(operationalState)

    case OnCompleteAction(timedTask) =>
      timedTask.callback()
      operationalState -= timedTask.associatedAction
      zoneRef ! ActuatorStateChanged(operationalState)
  }

  override protected def dissociateBehaviour(zoneRef: ActorRef): Receive = {
    case OnCompleteAction(deferredTask) => operationalState -= deferredTask.associatedAction
  }

  private def dispatchActionsIfDefined(actions: Set[Action],
                                       operationalState: OperationalState,
                                       handler: ActionHandler): OperationalState = {
    actions.foldLeft(operationalState) {
      (prevState, action) => dispatchActionIfDefined(action, prevState, handler)
    }
  }

  private def dispatchActionIfDefined(action: Action, operationalState: OperationalState, handler: ActionHandler): OperationalState = {
    handler.lift((operationalState, action)).fold(operationalState) {
      pendingComplete =>
        scheduleTimedTask(pendingComplete)
        operationalState + action
    }
  }

  private def scheduleTimedTask(timedTask: TimedTask): Unit = {
    timers.startSingleTimer(timedTask.associatedAction, OnCompleteAction(timedTask), timedTask.delay)
  }
}

object ActuatorActor {

  case class ActuatorStateChanged(operationalState: OperationalState)

  private[actuator] case class OnCompleteAction(task: TimedTask)

  def apply(actuator: Actuator)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(actuator)
  def props(actuator: Actuator): Props = Props(new ActuatorActor(actuator))
}