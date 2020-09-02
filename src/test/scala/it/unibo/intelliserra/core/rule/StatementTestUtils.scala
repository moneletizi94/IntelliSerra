package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.SimpleConditionStatement
import it.unibo.intelliserra.core.rule.dsl.{EqualsOperator, MajorOperator}
import it.unibo.intelliserra.utils.{TestActions, TestCategory}

trait StatementTestUtils extends TestActions with TestCategory {

  val temperatureValue = 20
  val humidityValue = 21
  val weatherValue = "sun"

  val temperatureStatement = SimpleConditionStatement(Temperature, MajorOperator, temperatureValue)
  val humidityStatement = SimpleConditionStatement(Humidity, MajorOperator, humidityValue)
  val weatherStatement = SimpleConditionStatement(Weather, EqualsOperator, weatherValue)
}
