package it.unibo.intelliserra.core.state

import it.unibo.intelliserra.core.actuator.{Action}
import it.unibo.intelliserra.core.sensor.Measure

trait State extends Serializable {
  def timestamp : Long
  def perceptions : List[Measure]
  def activeActions : List[Action]
}

object State{
  def apply(perceptions: List[Measure], activeActions : List[Action]): State = new StateImpl(perceptions, activeActions)
  def empty : State = StateImpl(List(),List())
}

case class StateImpl(override val perceptions: List[Measure], override val activeActions : List[Action]) extends State{
  override val timestamp : Long = System.currentTimeMillis / 1000
}