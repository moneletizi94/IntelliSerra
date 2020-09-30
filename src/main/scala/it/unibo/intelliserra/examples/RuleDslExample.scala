package it.unibo.intelliserra.examples

import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.perception.{Category, DoubleType, IntType}
import it.unibo.intelliserra.core.rule.Rule

// An example of rule dsl
object RuleDslExample extends App {

  import it.unibo.intelliserra.core.rule.dsl._

  case object Temperature extends Category[IntType]
  case object Humidity extends Category[DoubleType]

  case object Water extends Action
  case object Fan extends Action
  case class ActionWithArg(message: String) extends Action

  val ruleSingleAction: Rule = Temperature > 20 && Humidity =:= 20.3 execute Water
  val ruleMultipleAction: Rule = Temperature > 20 && Humidity < 20.3 executeMany Set(Water)

  println(ruleSingleAction)
  println(ruleMultipleAction)
}
