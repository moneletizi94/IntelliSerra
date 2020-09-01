package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.SimpleConditionStatement
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.sensor.Category
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RuleSpec extends WordSpecLike with Matchers {

  private object Water extends Action
  private object OpenWindow extends Action

  private object Temperature extends Category
  private object Humidity extends Category
  private object Weather extends Category

  private val temperatureValue = 20
  private val humidityValue = 21
  private val weatherValue = "sun"
  private val temperatureStatement = SimpleConditionStatement(Temperature, MajorOperator, temperatureValue)
  private val humidityStatement = SimpleConditionStatement(Humidity, MajorOperator, humidityValue)
  private val weatherStatement = SimpleConditionStatement(Weather, EqualsOperator, weatherValue)


  "A rule" should {

    "contain statement and action" in {
      val rule = Temperature > temperatureValue execute Water
      checkConditionComposition(rule)(temperatureStatement)
      rule.actions should contain only Water
    }

    "contain logical AND statement" in {
      val rule = Temperature > temperatureValue && Humidity > humidityValue && Weather =:= weatherValue execute Water
      checkConditionComposition(rule)(temperatureStatement, humidityStatement, weatherStatement)
    }

    "have unique statement per category" in {
      val rule = Temperature > temperatureValue && Temperature > temperatureValue execute Water
      ConditionStatement.toSimpleStatement(rule.condition) should have size 1
    }

    "have unique actions" in {
      val rule = Temperature > temperatureValue executeMany Set(Water, Water, OpenWindow)
      rule.actions should contain only (Water, OpenWindow)
    }

  }

  private def checkConditionComposition(rule: Rule)(statements: SimpleConditionStatement*): Unit = {
    ConditionStatement.toSimpleStatement(rule.condition) should contain only (statements:_*)
  }
}
