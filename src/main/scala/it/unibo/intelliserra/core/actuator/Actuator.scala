package it.unibo.intelliserra.core.actuator

import it.unibo.intelliserra.core.entity.ActingCapability

trait Actuator {
  def identifier: String
  def capability: ActingCapability
  def state: OperationalState
  def doAction(action : Action)
}
