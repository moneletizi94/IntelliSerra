package it.unibo.intelliserrademo

import it.unibo.intelliserrademo.CategoriesAndActions.{AirTemperature, DayNight, Dehumidifies, Fan, Heat, Humidity, SoilMoisture, Water}
import it.unibo.intelliserra.core.rule.dsl._

import scala.concurrent.duration._
/**
 * Some rules for tomato greenhouse. Extracted from https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6111376/
 */
object TomatoRules {

  val rules = List(
    DayNight =:= "day" && AirTemperature > 30.0 execute Fan(5 seconds), // bad fertilization, start fan for reduce temperature
    DayNight =:= "night" && AirTemperature < 10.0 execute Heat(3 seconds), // fertilization problem, start heating
    SoilMoisture < 45.0 execute Water(10 seconds), // water stress condition
    Humidity < 50.0 execute Dehumidifies(false),
    Humidity > 70.0 execute Dehumidifies(true)
  )

}
