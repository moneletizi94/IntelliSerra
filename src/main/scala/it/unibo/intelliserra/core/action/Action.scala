package it.unibo.intelliserra.core.action

import scala.concurrent.duration.FiniteDuration

/**
 * Represents the concept of action
 */
trait Action

/**
 * Represents an action that has a fixed duration
 */
trait TimedAction extends Action{
  /** The duration of the action */
  def time : FiniteDuration
}

/**
 * Represents a switchable action
 */
trait ToggledAction extends Action{
  /** The status of the action */
  def switchStatus : Boolean
}
