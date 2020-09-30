package it.unibo.intelliserra.examples.sensor

import it.unibo.intelliserra.core.perception.{Category, StringType}
import it.unibo.intelliserra.device.core.sensor.Sensor
import it.unibo.intelliserra.examples.sensor.OpenWeatherService.Location.LocationRequest

import scala.concurrent.duration.FiniteDuration

object WeatherSensor {
  case object Weather extends Category[StringType]

  def apply(sensorName: String, updateInterval: FiniteDuration, locationRequest: LocationRequest): Sensor =
    fromWeatherService(sensorName, updateInterval, OpenWeatherService(locationRequest))

  def fromWeatherService(sensorName: String, updateInterval: FiniteDuration, service: WeatherService): Sensor =
    Sensor(sensorName, Weather, updateInterval)(service.asStream.map(weather => StringType(weather.state)))
}