package it.unibo.intelliserra.core.action

sealed trait OperationalState {
  def isDoing(action: Action): Boolean = this match {
    case DoingActions(actions) => actions.map(_.getClass).contains(action.getClass)
    case _ => false
  }

  def +(action: Action): OperationalState = this match {
    case Idle => OperationalState(Set(action))
    case DoingActions(actions) if !isDoing(action) => OperationalState(actions + action)
    case DoingActions(actions) => OperationalState(actions)
  }

  def -(action: Action): OperationalState = this match {
    case Idle => OperationalState()
    case DoingActions(actions) if isDoing(action) => OperationalState(actions - action)
    case DoingActions(actions) => OperationalState(actions)
  }
}

object OperationalState {
  def apply(actions: Set[Action]): OperationalState = if (actions.isEmpty) Idle else DoingActions(actions)
  def apply(action: Action, actions: Action*): OperationalState = apply(actions :+ action toSet)
  def apply(): OperationalState = Idle
}

final case class DoingActions(actions : Set[Action]) extends OperationalState
case object Idle extends OperationalState