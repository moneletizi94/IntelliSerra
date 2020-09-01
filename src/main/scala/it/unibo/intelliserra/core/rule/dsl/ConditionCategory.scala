package it.unibo.intelliserra.core.rule.dsl

import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.{ConditionValue, SimpleConditionStatement}
import it.unibo.intelliserra.core.sensor.Category

sealed trait ConditionCategory {
  def >(that: ConditionValue): ConditionStatement
  def >=(that: ConditionValue): ConditionStatement
  def <(that: ConditionValue): ConditionStatement
  def <=(that: ConditionValue): ConditionStatement
  def =:=(that: ConditionValue): ConditionStatement
  def =\=(that: ConditionValue): ConditionStatement
}

object ConditionCategory {
  final case class BasicConditionCategory(category: Category) extends ConditionCategory {
    def >(that: ConditionValue): ConditionStatement = mkStatement(MajorOperator, that)
    def >=(that: ConditionValue): ConditionStatement = mkStatement(MajorEqualsOperator, that)
    def <(that: ConditionValue): ConditionStatement = mkStatement(MinorOperator, that)
    def <=(that: ConditionValue): ConditionStatement = mkStatement(MinorEqualsOperator, that)
    def =:=(that: ConditionValue): ConditionStatement = mkStatement(EqualsOperator, that)
    def =\=(that: ConditionValue): ConditionStatement = mkStatement(NotEqualsOperator, that)

    private def mkStatement(operator: ConditionOperator, right: ConditionValue) =
      SimpleConditionStatement(category, operator, right)
  }
}