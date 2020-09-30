package it.unibo.intelliserra.core.rule.dsl

import it.unibo.intelliserra.core.perception.{Category, ValueType}
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.AtomicConditionStatement

object ConditionCategory {

  /**
   * A Category wrapper ("decorator") that enables to create a condition statement
   * @param category  the wrapped category
   * @tparam V        the type of value expressed by the category [[ValueType]]
   */
  final case class ConditionCategoryOps[V <: ValueType](category: Category[V]) {

    /**
     * Apply the major operator
     * @param that  the value that compose the condition
     * @return a condition statement with major operator
     */
    def >(that: V): ConditionStatement = mkStatement(MajorOperator, that)

    /**
     * Apply the major equals operator
     * @param that  the value that compose the condition
     * @return a condition statement with major equals operator
     */
    def >=(that: V): ConditionStatement = mkStatement(MajorEqualsOperator, that)

    /**
     * Apply the minor operator
     * @param that  the value that compose the condition
     * @return a condition statement with minor operator
     */
    def <(that: V): ConditionStatement = mkStatement(MinorOperator, that)

    /**
     * Apply the minor equals operator
     * @param that  the value that compose the condition
     * @return a condition statement with minor equals operator
     */
    def <=(that: V): ConditionStatement = mkStatement(MinorEqualsOperator, that)

    /**
     * Apply the equals operator
     * @param that  the value that compose the condition
     * @return a condition statement with equals operator
     */
    def =:=(that: V): ConditionStatement = mkStatement(EqualsOperator, that)

    /**
     * Apply the not equals operator
     * @param that  the value that compose the condition
     * @return a condition statement with not equals operator
     */
    def =\=(that: V): ConditionStatement = mkStatement(NotEqualsOperator, that)

    private def mkStatement(operator: ConditionOperator, right: V) =
      AtomicConditionStatement(category, operator, right)
  }
}