package it.unibo.intelliserra.core.actuator

import it.unibo.intelliserra.core.Device
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.Capability.ActingCapability

import scala.concurrent.duration.FiniteDuration

trait Actuator extends Device {
  override def capability: ActingCapability
  def actionHandler: ActionHandler
}

trait TimedTask {
  def associatedAction: Action
  def callback: () => Unit
  def delay: FiniteDuration
}

object TimedTask {
  import scala.concurrent.duration._

  def now(action: Action): TimedTask = TimedTask(action, 0 millis)(_ => { })

  def apply(action: Action, delay: FiniteDuration)(callback: Action => Unit): TimedTask =
    TimedTaskImpl(action, () => callback(action), delay)

  case class TimedTaskImpl(override val associatedAction: Action,
                           override val callback: () => Unit,
                           override val delay: FiniteDuration) extends TimedTask
}

object Actuator {

  type ActionHandler = PartialFunction[(OperationalState, Action), TimedTask]

  case class ActionCompleted(action: Action, callback: Action => Unit)
}
