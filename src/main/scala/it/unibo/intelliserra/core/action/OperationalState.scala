package it.unibo.intelliserra.core.action

/**
 * Represent the state of an actuator that contains the current active actions.
 * This could be Idle or DoingAction. Its immutable structure.
 */
sealed trait OperationalState {

  /**
   * Check if the operational state contains the action.
   * @param action the action to be verified by
   * @return true if is doing that action, false otherwise
   */
  def isDoing(action: Action): Boolean = this match {
    case DoingActions(actions) => actions.map(_.getClass).contains(action.getClass)
    case _ => false
  }

  /**
   * Add an action to state only if is not currently active.
   * @param action  action to be added by
   * @return  the new operational state
   */
  def +(action: Action): OperationalState = this match {
    case Idle => OperationalState(Set(action))
    case DoingActions(actions) if !isDoing(action) => OperationalState(actions + action)
    case DoingActions(actions) => OperationalState(actions)
  }

  /**
   * Remove and action to state only if is currently active.
   * @param action  action to be remove by
   * @return  the new operational state
   */
  def -(action: Action): OperationalState = this match {
    case Idle => OperationalState()
    case DoingActions(actions) if isDoing(action) => OperationalState(actions - action)
    case DoingActions(actions) => OperationalState(actions)
  }
}

/** Represent the operational state with some active actions */
final case class DoingActions(actions : Set[Action]) extends OperationalState

/** Represent the idle operational state with no active actions */
case object Idle extends OperationalState

object OperationalState {

  /** Create an [[Idle]] operational state */
  def apply(): OperationalState = Idle

  /**
   * Create an OperationalState from the specified active actions
   * @param actions the active actions
   * @return an [[Idle]] state if the set of action are empty, [[DoingActions]] if not.
   */
  def apply(actions: Set[Action]): OperationalState = if (actions.isEmpty) Idle else DoingActions(actions)

  /** Create a [[DoingActions]] operational state with specified actions */
  def apply(action: Action, actions: Action*): OperationalState = apply(actions :+ action toSet)
}
