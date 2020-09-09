package it.unibo.intelliserra.core.actuator

sealed trait OperationalState {
  def isDoing(action: Action): Boolean = this match {
    case DoingAction(actions) => actions.contains(action)
    case _ => false
  }

  def +(action: Action): OperationalState = this match {
    case DoingAction(actions) => OperationalState(actions + action)
    case Idle => OperationalState(Set(action))
  }

  def -(action: Action): OperationalState = this match {
    case DoingAction(actions) => OperationalState(actions - action)
    case Idle => OperationalState()
  }
}

object OperationalState {
  def apply(actions: Set[Action]): OperationalState = if (actions.isEmpty) Idle else DoingAction(actions)
  def apply(action: Action, actions: Action*): OperationalState = apply(actions :+ action toSet)
  def apply(): OperationalState = Idle
}

final case class DoingAction(actions : Set[Action]) extends OperationalState
case object Idle extends OperationalState