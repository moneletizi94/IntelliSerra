package it.unibo.intelliserra.core.state

import it.unibo.intelliserra.core.actuator.{Action, DoingAction, OperationalState}
import it.unibo.intelliserra.core.sensor.Measure

trait State {
  def timestamp : Long
  def perceptions : List[Measure]
  def activeActions : List[Action]
}

object State{
  def apply(perceptions: List[Measure], activeActions : List[Action]): State = new StateImpl(perceptions, activeActions)
}

case class StateImpl(override val perceptions: List[Measure], override val activeActions : List[Action]) extends State{
  override val timestamp : Long = System.currentTimeMillis / 1000
}