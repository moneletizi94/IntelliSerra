package it.unibo.intelliserra.core.state

import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.perception.Measure

/**
 *
 */
trait State extends Serializable {
  /**  represents the time when it was produced */
  def timestamp : Long

  /** represents a list of aggregated measures, one for each category */
  def perceptions : List[Measure]

  /** represents a list of active actions */
  def activeActions : List[Action]
}

object State{
  /**
   * Generate a new state from new perceptions and new active actions
   * @param perceptions The list of new perceptions
   * @param activeActions The list of new active actions
   * @return a new State
   */
  def apply(perceptions: List[Measure], activeActions : List[Action]): State = StateImpl(perceptions, activeActions)

  /**
   * Generate a new empty state
   * @return A state containing no measures and no actions
   */
  def empty : State = StateImpl(List(),List())
}

final case class StateImpl(override val perceptions: List[Measure], override val activeActions : List[Action]) extends State{
  override val timestamp : Long = System.currentTimeMillis / 1000
}