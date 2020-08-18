package it.unibo.intelliserra.core.state

import it.unibo.intelliserra.core.actuator.DoingAction
import it.unibo.intelliserra.core.sensor.Measure

trait State {
  def timestamp : Long
  def perceptions : List[Measure]
  def activeActions : List[DoingAction]
}