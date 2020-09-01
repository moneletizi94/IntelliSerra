package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement

trait Rule {
  def condition: ConditionStatement
  def actions: Set[Action]
}

object Rule {
  def apply(condition: ConditionStatement, actions: Set[Action]): Rule = RuleImpl(condition, actions)
  private case class RuleImpl(override val condition: ConditionStatement, override val actions: Set[Action]) extends Rule
}