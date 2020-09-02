package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.AtomicConditionStatement
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.sensor.{Category, IntType, StringType}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RuleSpec extends WordSpecLike with Matchers with StatementTestUtils {

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
      ConditionStatement.toAtomicStatements(rule.condition) should have size 1
    }

    "have unique actions" in {
      val rule = Temperature > temperatureValue executeMany Set(Water, Water, OpenWindow)
      rule.actions should contain only (Water, OpenWindow)
    }

  }

  private def checkConditionComposition(rule: Rule)(statements: AtomicConditionStatement*): Unit = {
    ConditionStatement.toAtomicStatements(rule.condition) should contain only (statements:_*)
  }
}
