package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement

trait Rule {
  /**
   * The condition statement of rule to be evaluated
   */
  def condition: ConditionStatement

  /**
   * A set to be executed if the evaluation result of statement is true
   */
  def actions: Set[Action]
}

object Rule {

  /**
   * Create a new rule with statement and actions
   * @param condition the condition statement of rule
   * @param actions   the action's set of rule
   * @return a new instance of rule
   */
  def apply(condition: ConditionStatement, actions: Set[Action]): Rule = RuleImpl(condition, actions)
  private case class RuleImpl(override val condition: ConditionStatement, override val actions: Set[Action]) extends Rule
}