package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, CharType, DoubleType, IntType, Measure, StringType, ValueType}
import it.unibo.intelliserra.core.state.State

object Representations {

  implicit object ValueTypeToProlog extends PrologRepresentation[ValueType] {
    override def toTerm(data: ValueType): Term = Term.createTerm(valueToTerm(data))

    private def valueToTerm(measureValue: ValueType): String = measureValue match {
      case StringType(value) => s"string($value)"
      case IntType(value) => value.toString
      case DoubleType(value) => value.toString
      case CharType(value) => value.toInt.toString
      case BooleanType(value) => if (value) "1" else "0"
    }
  }

  implicit object CategoryToProlog extends PrologRepresentation[Category[ValueType]] {
    override def toTerm(data: Category[ValueType]): Term =
      Term.createTerm(data.getClass.getSimpleName.split('$').head.toLowerCase)
  }

  implicit object StateToProlog extends PrologRepresentation[State] {

    override def toTerm(data: State): Term = Struct.list(data.perceptions.map(measureToTerm): _*)

    private def measureToTerm(measure: Measure): Term =
      Struct.of("measure", measure.value.toTerm, measure.category.toTerm)
  }

  case object RulePrologRepresentation extends PrologRepresentation[Rule]{
    override def toTerm(data: Rule): Struct = {
      //data.actions.map(p => Struct.rule(p.toTerm,data.condition.toTerm)
      Struct.atom("")
    }
  }
  case object ActionPrologRepresentation extends PrologRepresentation[Action] {
    override def toTerm(data: Action): Struct = Struct.atom(s"action(${data.getClass.getSimpleName.split('$').head.toLowerCase})")
  }

  implicit case object ConditionStatementPrologRepresentation extends PrologRepresentation[ConditionStatement] {
    var variable = "X"
    override def toTerm(data: ConditionStatement): Struct = ??? /*data match {
      case ConditionStatement.AtomicConditionStatement(left, operator, right) => s"measure(${variable},${left.toTerm}),${variable}
      case ConditionStatement.AndConditionStatement(statements) => statements.toTerm
    }*/
  }
}
