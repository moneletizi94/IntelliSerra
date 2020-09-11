package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term, Var}
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.sensor.{Category, ValueType}
import ConditionStatement._
import it.unibo.intelliserra.examples.RuleDslExample._

object Representations {
  case object RulePrologRepresentation extends PrologRepresentation[Rule]{
    override def toTerm(data: Rule): Term = {
       Struct.list(data.actions.map(action => Struct.rule(action.toTerm,data.condition.toTerm)).toList:_*)
    }
  }

  case object ActionPrologRepresentation extends PrologRepresentation[Action] {
    override def toTerm(data: Action): Term = Struct.atom(s"action(${data.getClass.getSimpleName.split('$').head.toLowerCase})")
  }

  case object CategoryPrologRepresentation extends PrologRepresentation[Category[ValueType]]{
    override def toTerm(data: Category[ValueType]): Term = Term.createTerm(s"${data.getClass.getSimpleName.split('$').head.toLowerCase}")
  }
  case object ValueTypePrologRepresentation extends PrologRepresentation[ValueType] {
    override def toTerm(data: ValueType): Term = Term.createTerm("cazziloro")
  }

  case object ConditionStatementPrologRepresentation extends PrologRepresentation[ConditionStatement] {
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