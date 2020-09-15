package it.unibo.intelliserra.examples.sensor

import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor, StringType}
import it.unibo.intelliserra.examples.sensor.OpenWeatherService.Location.LocationRequest
import it.unibo.intelliserra.examples.sensor.WeatherSensor.Weather

import scala.concurrent.duration.FiniteDuration

object WeatherSensor {
  case object Weather extends Category[StringType]

  def apply(sensorName: String, updateInterval: FiniteDuration, locationRequest: LocationRequest): WeatherSensor =
    fromWeatherService(sensorName, updateInterval, OpenWeatherService(locationRequest))

  def fromWeatherService(sensorName: String, updateInterval: FiniteDuration, service: WeatherService): WeatherSensor =
    new WeatherSensor(sensorName, updateInterval, service)
}

private[sensor] class WeatherSensor(override val identifier: String,
                                    override val readPeriod: FiniteDuration,
                                    private val weatherService: WeatherService) extends Sensor {

  override def capability: Capability.SensingCapability = Capability.sensing(Weather)

  override def read(): Option[Measure] = {
    weatherService.currentWeather().map {
      weatherData => Measure(Weather)(weatherData.state)
    }
  }

  override def onInit(): Unit = ()
  override def onAssociateZone(zoneName: String): Unit = ()
  override def onDissociateZone(zoneName: String): Unit = ()
}
