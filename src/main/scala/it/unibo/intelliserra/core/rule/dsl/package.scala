package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.perception.{Category, ValueType}
import it.unibo.intelliserra.core.rule.dsl.ConditionCategory.ConditionCategoryOps

package object dsl {

  /**
   * Implicit conversion from Category to CategoryOps for enable the statement operations
   * @param category  the category to be wrapped
   * @tparam V        the type of value expressed by the category [[ValueType]]
   * @return an enriched category that support the statement operations
   */
  implicit def categoryToConditionCategoryOps[V <: ValueType](category: Category[V]): ConditionCategoryOps[V] = ConditionCategoryOps(category)

  /**
   * Pimping the ConditionStatement in order to create a Rule
   * @param statement the statement should be used for create a rule
   */
  implicit class RichStatement(statement: ConditionStatement) {

    /**
     * Create a new rule with multiple triggerable actions
     * @param actions the set of actions to be triggered
     * @return  a new rule using this statement as rule condition's
     */
    def executeMany(actions: => Set[Action]): Rule = Rule(statement, actions)

    /**
     * Create a new rule with a single triggerable action
     * @param action  the action to be triggered
     * @return  a new rule using this statement as rule condition's
     */
    def execute(action: => Action): Rule = statement.executeMany(Set(action))
  }
}
