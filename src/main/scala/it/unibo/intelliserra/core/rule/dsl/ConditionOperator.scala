package it.unibo.intelliserra.core.rule.dsl

/**
 * It defines an operator used for describe a condition statement [[ConditionStatement]]
 */
sealed trait ConditionOperator
case object MajorOperator extends ConditionOperator
case object MajorEqualsOperator extends ConditionOperator
case object MinorOperator extends ConditionOperator
case object MinorEqualsOperator extends ConditionOperator
case object EqualsOperator extends ConditionOperator
case object NotEqualsOperator extends ConditionOperator
