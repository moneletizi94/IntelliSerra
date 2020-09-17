package it.unibo.intelliserrademo.customdevice

import akka.dispatch.ExecutionContexts
import it.unibo.intelliserra.core.Device
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserrademo.customdevice.TomatoSensors.{AirTemperatureSensor, DayNightSensor, SoilMoistureSensor, WeatherSensor}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
object TomatoDeviceApp extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContexts.global()

  val Hostname = "localhost"
  val Port = 8082
  val GreenhouseName = "SerraDiPomodori"

  val deviceClient = DeviceDeploy(GreenhouseName, Hostname, Port)

  val sensorMap: List[(Stream[Sensor], Int)] = List(
    Device.generateWithName("weather")(WeatherSensor.apply) -> 10,
    Device.generateWithName("airtemp")(AirTemperatureSensor.apply) -> 10,
    Device.generateWithName("airhum")(AirTemperatureSensor.apply) -> 10,
    Device.generateWithName("soilmoisture")(SoilMoistureSensor.apply) -> 5,
    Device.generateWithName("daynight")(DayNightSensor.apply) -> 5
  )

  for {
    deviceEntry <- sensorMap
    sensor <- deviceEntry._1.take(deviceEntry._2)
  } debugJoin(deviceClient join sensor)

  private def debugJoin(future: Future[String]): Unit = {
    future.onComplete {
      case Failure(exception) => println(exception)
      case Success(value) => println(s"Success join $value")
    }
  }
}
