package it.unibo.intelliserra.core.rule.dsl

import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.SimpleConditionStatement
import it.unibo.intelliserra.core.sensor.{Category, ValueType}

sealed trait ConditionCategory[ConditionValue <: ValueType] {
  def >(that: ConditionValue): ConditionStatement
  def >=(that: ConditionValue): ConditionStatement
  def <(that: ConditionValue): ConditionStatement
  def <=(that: ConditionValue): ConditionStatement
  def =:=(that: ConditionValue): ConditionStatement
  def =\=(that: ConditionValue): ConditionStatement
}

object ConditionCategory {

  final case class ConditionCategoryOps[V <: ValueType](category: Category[V]) extends ConditionCategory[V] {
    def >(that: V): ConditionStatement = mkStatement(MajorOperator, that)
    def >=(that: V): ConditionStatement = mkStatement(MajorEqualsOperator, that)
    def <(that: V): ConditionStatement = mkStatement(MinorOperator, that)
    def <=(that: V): ConditionStatement = mkStatement(MinorEqualsOperator, that)
    def =:=(that: V): ConditionStatement = mkStatement(EqualsOperator, that)
    def =\=(that: V): ConditionStatement = mkStatement(NotEqualsOperator, that)

    private def mkStatement(operator: ConditionOperator, right: V) =
      SimpleConditionStatement(category, operator, right)
  }
}