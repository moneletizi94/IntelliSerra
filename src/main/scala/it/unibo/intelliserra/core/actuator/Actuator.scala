package it.unibo.intelliserra.core.actuator

import it.unibo.intelliserra.core.entity.Entity

trait Actuator extends Entity {
  def state: OperationalState
  def doAction(action : Action)
}
