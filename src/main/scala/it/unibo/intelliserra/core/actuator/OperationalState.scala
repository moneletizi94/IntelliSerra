package it.unibo.intelliserra.core.actuator

sealed trait OperationalState {
  def +(action: Action): OperationalState = this match {
    case DoingAction(actions) => OperationalState(actions + action)
    case Idle => OperationalState()
  }

  def -(action: Action): OperationalState = this match {
    case DoingAction(actions) => OperationalState(actions - action)
    case Idle => OperationalState()
  }
}

object OperationalState {
  def apply(actions: Set[Action]): OperationalState =
    if (actions.isEmpty) Idle else DoingAction(actions)
  def apply(): OperationalState = Idle
}

final case class DoingAction(actions : Set[Action]) extends OperationalState
case object Idle extends OperationalState