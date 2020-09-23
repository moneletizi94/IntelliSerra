package it.unibo.intelliserra.core.action

import scala.concurrent.duration.FiniteDuration

trait Action
trait TimedAction extends Action{
  def time : FiniteDuration
}
trait ToggledAction extends Action{
  def switchStatus : Boolean
}
trait ActionWithAnyParams extends Action{
  def param : Any
}
