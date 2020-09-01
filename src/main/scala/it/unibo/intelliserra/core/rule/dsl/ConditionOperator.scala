package it.unibo.intelliserra.core.rule.dsl

sealed trait ConditionOperator
case object MajorOperator extends ConditionOperator
case object MajorEqualsOperator extends ConditionOperator
case object MinorOperator extends ConditionOperator
case object MinorEqualsOperator extends ConditionOperator
case object EqualsOperator extends ConditionOperator
case object NotEqualsOperator extends ConditionOperator
