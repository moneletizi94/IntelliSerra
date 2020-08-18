package it.unibo.intelliserra.core.actuator

sealed trait OperationalState

final case class DoingAction(action : Action) extends OperationalState
case object Idle extends OperationalState