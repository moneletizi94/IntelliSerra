package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionCategory.BasicConditionCategory

package object dsl {

  import it.unibo.intelliserra.core.sensor._

  implicit def categoryToConditionCategory(category: Category): ConditionCategory = BasicConditionCategory(category)

  implicit class RichStatement(statement: ConditionStatement) {
    def executeMany(actions: => Set[Action]): Rule = Rule(statement, actions)
    def execute(action: => Action): Rule = statement.executeMany(Set(action))
  }
}
