package it.unibo.intelliserra.core.actuator

import scala.concurrent.duration.FiniteDuration

trait Action
trait TimedAction extends Action{
  def time : FiniteDuration
}

