package it.unibo.intelliserra.core.actuator

import it.unibo.intelliserra.core.Device
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.Capability.ActingCapability

import scala.concurrent.duration.FiniteDuration

trait Actuator extends Device {
  override def capability: ActingCapability
  def actionHandler: ActionHandler
}

object Actuator {
  type ActionHandler = PartialFunction[(OperationalState, Action), TimedTask]
}
