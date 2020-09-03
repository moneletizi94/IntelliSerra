package it.unibo.intelliserra.core.state

import it.unibo.intelliserra.core.actuator.DoingAction
import it.unibo.intelliserra.core.sensor.Measure

trait State {
  def timestamp : Long
  def perceptions : List[Measure]
  def activeActions : List[DoingAction]
}

object State{
  def apply(perceptions: List[Measure], activeActions : List[DoingAction]): State = new StateImpl(perceptions, activeActions)
}

case class StateImpl(override val perceptions: List[Measure], override val activeActions : List[DoingAction]) extends State{
  override val timestamp : Long = System.currentTimeMillis / 1000
}