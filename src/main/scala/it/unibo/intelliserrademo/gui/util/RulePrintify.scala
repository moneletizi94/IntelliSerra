package it.unibo.intelliserrademo.gui.util

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.{AndConditionStatement, AtomicConditionStatement, toAtomicStatements}
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.sensor._

object RulePrintify {

  private[gui] def rulePrintify(rule: Rule): String = {
    conditionPrintify(rule.condition) + " -> " + actionPrintify(rule.actions)
  }

  private def conditionPrintify(condition: ConditionStatement): String = condition match {
    case AtomicConditionStatement(left, operator, right) => left + " " +  operatorPrintify(operator) + " " + valueTypePrintify(right)
    case compound : AndConditionStatement =>
      toAtomicStatements(compound).map(conditionPrintify).mkString(" && ")
  }

  private def actionPrintify(actions: Set[Action]): String = {
    actions.map(identity).mkString(", ")
  }

  private def operatorPrintify(operator: ConditionOperator): String = operator match {
    case MajorOperator => ">"
    case MajorEqualsOperator => ">="
    case MinorOperator => "<"
    case MinorEqualsOperator => "<="
    case EqualsOperator => "=="
    case NotEqualsOperator => "!="
  }

  private def valueTypePrintify(valueType: ValueType): String = valueType match {
    case StringType(value) => value
    case IntType(value) => value.toString
    case DoubleType(value) => value.toString
    case CharType(value) => value.toInt.toString
    case BooleanType(value) => value.toString
  }
}
