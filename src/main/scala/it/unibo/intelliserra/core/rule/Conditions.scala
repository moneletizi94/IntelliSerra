package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.ConditionStatement.SimpleConditionStatement


trait ConditionCategory {
  def name: String
}
object ConditionCategory {
  def apply(name: String): ConditionCategory with ConditionOperations = ConditionCategoryWithOps(name)
  private case class ConditionCategoryWithOps(override val name: String) extends ConditionCategory with ConditionOperations
}

trait ConditionOperations extends ConditionCategory {
  def >(that: ConditionValue): ConditionStatement = mkStatement(MajorOperator, that)
  def >=(that: ConditionValue): ConditionStatement = mkStatement(MajorEqualsOperator, that)
  def <(that: ConditionValue): ConditionStatement = mkStatement(MinorOperator, that)
  def <=(that: ConditionValue): ConditionStatement = mkStatement(MinorEqualsOperator, that)
  def ==(that: ConditionValue): ConditionStatement = mkStatement(EqualsOperator, that)
  def !=(that: ConditionValue): ConditionStatement = mkStatement(NotEqualsOperator, that)

  private def mkStatement(operator: ConditionOperator, right: ConditionValue) =
    SimpleConditionStatement(this, operator, right)
}

sealed trait ConditionStatement {
  def &&(statement: ConditionStatement): ConditionStatement
  def to(actions: Set[Action]): Rule
}
object ConditionStatement {
  case class SimpleConditionStatement(left: ConditionCategory, operator: ConditionOperator, right: ConditionValue) extends ConditionStatement {
    override def &&(statement: ConditionStatement): ConditionStatement = ComplexConditionStatement(Set(this, statement))
    override def to(actions: Set[Action]): Rule = Rule(this, actions)
  }

  case class ComplexConditionStatement(statements: Set[ConditionStatement]) extends ConditionStatement {
    override def &&(statement: ConditionStatement): ConditionStatement = ComplexConditionStatement(statements + statement)
    override def to(actions: Set[Action]): Rule = Rule(this, actions)
  }
}

sealed trait ConditionOperator
case object MajorOperator extends ConditionOperator
case object MajorEqualsOperator extends ConditionOperator
case object MinorOperator extends ConditionOperator
case object MinorEqualsOperator extends ConditionOperator
case object EqualsOperator extends ConditionOperator
case object NotEqualsOperator extends ConditionOperator


sealed trait ConditionValue
final case class IntValue(value: Int) extends ConditionValue
final case class DoubleValue(value: Double) extends ConditionValue
final case class BooleanValue(value: Boolean) extends ConditionValue
final case class CharValue(value: Char) extends ConditionValue
final case class StringValue(value: String) extends ConditionValue

object Example extends App {
  case object Water extends Action
  case object Water2 extends Action

  val rule = ConditionCategory("bio") > IntValue(2) && ConditionCategory("bio2") > IntValue(2) to Set(Water, Water2)
  println(rule)
}
