package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.AtomicConditionStatement
import it.unibo.intelliserra.core.rule.dsl.{EqualsOperator, MajorOperator}
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature, Weather}

trait StatementTestUtils {

  val temperatureValue = 20
  val humidityValue = 21
  val weatherValue = "sun"

  val temperatureStatement = AtomicConditionStatement(Temperature, MajorOperator, temperatureValue)
  val humidityStatement = AtomicConditionStatement(Humidity, MajorOperator, humidityValue)
  val weatherStatement = AtomicConditionStatement(Weather, EqualsOperator, weatherValue)
}
