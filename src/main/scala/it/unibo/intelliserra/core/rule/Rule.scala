package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.ConditionStatement.{ComplexConditionStatement, SimpleConditionStatement}
import it.unibo.intelliserra.core.sensor.Category

import scala.util.{Failure, Success, Try}

/**
 * Rule grammar
 *
 * RULES -> RULE | RULE LOGICOP RULE
 * RULE -> CATEGORY CONDOP CONDVALUE | RULES
 *
 * LOGICOP -> &&
 * CATEGORY -> "name"
 * CONDOP -> > | < | == | !=
 * CONDVALUE -> int | double | string | char | boolean
 */

trait Rule /* ActivationRule */{
  def condition: ConditionStatement
  def actions: Set[Action]
}
object Rule {
  def apply(condition: ConditionStatement, actions: Set[Action]): Rule = RuleImpl(condition, actions)
  private case class RuleImpl(override val condition: ConditionStatement, override val actions: Set[Action]) extends Rule
}