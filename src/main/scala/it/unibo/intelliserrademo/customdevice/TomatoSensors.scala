package it.unibo.intelliserrademo.customdevice

import it.unibo.intelliserrademo.common.CategoriesAndActions.{AirTemperature, DayNight, Humidity, SoilMoisture, Weather}
import SimulatedDevice.CustomSensor
import it.unibo.intelliserra.core.perception.{DoubleType, StringType}
import it.unibo.intelliserrademo.common.Generator
import it.unibo.intelliserrademo.common.Simulation.{DoubleSinSample, StringSample}

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

  object AirHumiditySensor {
    private val StdValue = 50
    private val Delta = 25

    def apply(name: String): CustomSensor[DoubleType] =
      CustomSensor(name, 5 seconds, Humidity)(Generator.generateStream(DoubleSinSample(StdValue, Delta)))
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
