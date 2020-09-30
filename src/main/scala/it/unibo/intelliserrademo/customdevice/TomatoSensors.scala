package it.unibo.intelliserrademo.customdevice

import it.unibo.intelliserrademo.common.CategoriesAndActions.{AirTemperature, DayNight, Humidity, SoilMoisture, Weather}
import it.unibo.intelliserra.core.perception.{DoubleType, StringType}
import it.unibo.intelliserra.device.core.sensor.Sensor
import it.unibo.intelliserrademo.common.Generator
import it.unibo.intelliserrademo.common.Simulation.{DoubleSinSample, StringSample}

import scala.concurrent.duration._

/**
 * Some useful sensors in tomato greenhouse.
 * The sensor's values are simulated.
 */
object TomatoSensors {

  object WeatherSensor {
    def apply(name: String): Sensor =
      Sensor(name, Weather, 5 seconds)(Generator.generateStream(StringSample("sun" -> 0.8, "rain" -> 0.2)))
  }

  object AirTemperatureSensor {
    private val StdValue = 30
    private val Delta = 2

    def apply(name: String): Sensor =
      Sensor(name, AirTemperature, 5 seconds)(Generator.generateStream(DoubleSinSample(StdValue, Delta)))
  }

  object AirHumiditySensor {
    private val StdValue = 50
    private val Delta = 25

    def apply(name: String): Sensor =
      Sensor(name, Humidity, 5 seconds)(Generator.generateStream(DoubleSinSample(StdValue, Delta)))
  }

  object SoilMoistureSensor {
    private val StdValue = 80
    private val Delta = 20

    def apply(name: String): Sensor =
      Sensor(name, SoilMoisture, 5 seconds)(Generator.generateStream(DoubleSinSample(StdValue, Delta)))
  }

  object DayNightSensor {
    def apply(name: String): Sensor =
      Sensor(name, DayNight, 5 seconds)(Generator.generateStream(StringSample("day" -> 1.0)))
  }
}
