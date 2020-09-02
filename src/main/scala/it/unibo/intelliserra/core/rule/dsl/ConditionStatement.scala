package it.unibo.intelliserra.core.rule.dsl

import it.unibo.intelliserra.core.sensor.{Category, ValueType}

sealed trait ConditionStatement {
  def &&(statement: ConditionStatement): ConditionStatement
}

object ConditionStatement {

  final case class SimpleConditionStatement(left: Category[ValueType], operator: ConditionOperator, right: ValueType) extends ConditionStatement {
    override def &&(statement: ConditionStatement): ConditionStatement = AndConditionStatement(Set(this, statement))
  }

  final case class AndConditionStatement(statements: Set[ConditionStatement]) extends ConditionStatement {
    override def &&(statement: ConditionStatement): ConditionStatement = AndConditionStatement(statements + statement)
  }

  def toSimpleStatement(conditionStatement: ConditionStatement): List[SimpleConditionStatement] = conditionStatement match {
    case simple: SimpleConditionStatement => List(simple)
    case AndConditionStatement(statements) => statements.flatMap(toSimpleStatement).toList
  }
}