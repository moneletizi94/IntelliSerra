package it.unibo.intelliserra.core.rule.dsl

import it.unibo.intelliserra.core.sensor.{Category, ValueType}

/**
 * It define a condition statement used for describe
 * the activation conditions of a rule [[it.unibo.intelliserra.core.rule.Rule]]
 */
sealed trait ConditionStatement {

  /**
   * Apply logical AND operator for compose a new statement
   * @param statement   the statement that should be put in AND.
   * @return a new statement that represent the AND between this statement and another
   */
  def &&(statement: ConditionStatement): ConditionStatement
}

object ConditionStatement {

  /**
   * It describe an atomic condition statement (e.g. ABC > 20)
   * @param left      the left member of statement
   * @param operator  the operator of statement
   * @param right     the right member of statement
   */
  final case class AtomicConditionStatement(left: Category[ValueType], operator: ConditionOperator, right: ValueType) extends ConditionStatement {
    override def &&(statement: ConditionStatement): ConditionStatement = AndConditionStatement(Set(this, statement))
  }

  /**
   * It describe a set of condition statement connected with AND operator
   * @param statements  the set of statement connected with AND.
   */
  final case class AndConditionStatement(statements: Set[ConditionStatement]) extends ConditionStatement {
    override def &&(statement: ConditionStatement): ConditionStatement = AndConditionStatement(statements + statement)
  }

  /**
   * Extract all atomic statements from a general condition statement
   * @param conditionStatement  the condition statement that should be extracted to
   * @return  a list of simple statement
   */
  def toAtomicStatements(conditionStatement: ConditionStatement): List[AtomicConditionStatement] = conditionStatement match {
    case simple: AtomicConditionStatement => List(simple)
    case AndConditionStatement(statements) => statements.flatMap(toAtomicStatements).toList
  }
}