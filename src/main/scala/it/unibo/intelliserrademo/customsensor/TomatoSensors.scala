package it.unibo.intelliserrademo.customsensor

import it.unibo.intelliserra.core.sensor.{DoubleType, StringType}
import it.unibo.intelliserrademo.CategoriesAndActions.{AirTemperature, DayNight, SoilMoisture, Weather}
import it.unibo.intelliserrademo.Generator
import SimulatedDevice.CustomSensor
import it.unibo.intelliserrademo.Simulation.{DoubleSinSample, StringSample}
import scala.concurrent.duration._

/**
 * Some useful sensors in tomato greenhouse.
 * The sensor's values are simulated.
 */
object TomatoSensors {

  object WeatherSensor {
    def apply(name: String): CustomSensor[StringType] =
      CustomSensor(name, 5 seconds, Weather)(Generator.generateStream(StringSample("sun" -> 0.8, "rain" -> 0.2)))
  }

  object AirTemperatureSensor {
    private val StdValue = 30
    private val Delta = 2

    def apply(name: String): CustomSensor[DoubleType] =
      CustomSensor(name, 5 seconds, AirTemperature)(Generator.generateStream(DoubleSinSample(StdValue, Delta)))
  }

  object SoilMoistureSensor {
    private val StdValue = 80
    private val Delta = 20

    def apply(name: String): CustomSensor[DoubleType] =
      CustomSensor(name, 5 seconds, SoilMoisture)(Generator.generateStream(DoubleSinSample(StdValue, Delta)))
  }

  object DayNightSensor {
    def apply(name: String): CustomSensor[StringType] =
      CustomSensor(name, 5 seconds, DayNight)(Generator.generateStream(StringSample("day" -> 1.0)))
  }
}
