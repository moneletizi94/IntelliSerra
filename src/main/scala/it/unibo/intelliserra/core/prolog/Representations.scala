package it.unibo.intelliserra.core.prolog

import alice.tuprolog.Struct
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement
import it.unibo.intelliserra.core.sensor.Category

object Representations {
  case object RulePrologRepresentation extends PrologRepresentation[Rule]{
    override def toTerm(data: Rule): Struct = {
      //data.actions.map(p => Struct.rule(p.toTerm,data.condition.toTerm)
      Struct.atom("")
    }
  }

  case object ActionPrologRepresentation extends PrologRepresentation[Action] {
    override def toTerm(data: Action): Struct = Struct.atom(s"action(${data.getClass.getSimpleName.split('$').head.toLowerCase})")
  }

  case object CategoryPrologRepresentation extends PrologRepresentation[Category[_]]{
    override def toTerm(data: Category[_]): Struct = Struct.atom(s"${data.getClass.getSimpleName.split('$').head.toLowerCase}")
  }

  implicit case object ConditionStatementPrologRepresentation extends PrologRepresentation[ConditionStatement] {
    var variable = "X"
    override def toTerm(data: ConditionStatement): Struct = ??? /*data match {
      case ConditionStatement.AtomicConditionStatement(left, operator, right) => s"measure(${variable},${left.toTerm}),${variable}
      case ConditionStatement.AndConditionStatement(statements) => statements.toTerm
    }*/
  }
}
