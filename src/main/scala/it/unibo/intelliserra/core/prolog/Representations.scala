package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, CharType, DoubleType, IntType, Measure, StringType, ValueType}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.sensor.{Category, ValueType}
import ConditionStatement._
import it.unibo.intelliserra.examples.RuleDslExample._

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
  implicit object RulePrologRepresentation extends PrologRepresentation[Rule]{
    override def toTerm(data: Rule): Term = {
       Struct.list(data.actions.map(action => Struct.rule(action.toTerm,data.condition.toTerm)).toList:_*)
    }
  }

  implicit object ActionPrologRepresentation extends PrologRepresentation[Action] {
    override def toTerm(data: Action): Term = Struct.atom(s"action(${data.getClass.getSimpleName.split('$').head.toLowerCase})")
  }

  implicit object StateToProlog extends PrologRepresentation[State] {

    override def toTerm(data: State): Term = Struct.list(data.perceptions.map(measureToTerm): _*)

    private def measureToTerm(measure: Measure): Term =
      Struct.of("measure", measure.value.toTerm, measure.category.toTerm)
  }

  implicit object ConditionStatementPrologRepresentation extends PrologRepresentation[ConditionStatement] {
    private val counter = Stream.from(0).iterator
    override def toTerm(data: ConditionStatement): Term = {
      data match {
        case AtomicConditionStatement(left, operator, right) =>
          val actualCounter = counter.next
          Term.createTerm(s"measure(X${actualCounter},${left.toTerm}),X${actualCounter} ${operatorToProlog(operator)} ${right.toTerm}")
        case AndConditionStatement(statements) => Struct.list(statements.map(statement => toTerm(statement)).toList:_*) //TODO serve una virgola ?
      }
    }
    def operatorToProlog(operator: ConditionOperator): String = operator match {
      case MajorOperator => ">"
      case MajorEqualsOperator => ">="
      case MinorOperator =>"<"
      case MinorEqualsOperator => "=<"
      case EqualsOperator => "="
      case NotEqualsOperator => "\\="
    }
  }
}

object Prova extends App {
  val simpleRule = Temperature > 10 execute Water
  val compositeRule = Temperature > 20 && Humidity > 50.0 executeMany Set(Water, Fan)
  println(simpleRule.toTerm)
  println(compositeRule.toTerm)
}