package it.unibo.intelliserra.device.core

import it.unibo.intelliserra.core.action.{Action, OperationalState}
import it.unibo.intelliserra.core.entity.Capability.ActingCapability
import it.unibo.intelliserra.core.entity.Device
import it.unibo.intelliserra.device.core.Actuator.ActionHandler

trait Actuator extends Device with DeviceCallback {
  override def capability: ActingCapability
  def actionHandler: ActionHandler
}

object Actuator {
  type ActionHandler = PartialFunction[(OperationalState, Action), TimedTask]
}
