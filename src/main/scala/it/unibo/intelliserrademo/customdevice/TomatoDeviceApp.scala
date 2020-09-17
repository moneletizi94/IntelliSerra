package it.unibo.intelliserrademo.customdevice

import akka.dispatch.ExecutionContexts
import it.unibo.intelliserra.core.Device
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserrademo.customdevice.TomatoActuators.{Dehumidifiers, FanActuator, HeatActuator, WaterActuator}
import it.unibo.intelliserrademo.customdevice.TomatoSensors.{AirHumiditySensor, AirTemperatureSensor, DayNightSensor, SoilMoistureSensor, WeatherSensor}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
object TomatoDeviceApp extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContexts.global()

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "SerraDiPomodori"

  val deviceClient = DeviceDeploy(GreenhouseName, Hostname, Port)

  createSensors(List(
    Device.generateWithName("weather")(WeatherSensor.apply) -> 10,
    Device.generateWithName("airtemp")(AirTemperatureSensor.apply) -> 10,
    Device.generateWithName("airhum")(AirHumiditySensor.apply) -> 10,
    Device.generateWithName("soilmoisture")(SoilMoistureSensor.apply) -> 5,
    Device.generateWithName("daynight")(DayNightSensor.apply) -> 5
  ))

  createActuators(List(
    Device.generateWithName("wateractuator")(WaterActuator.apply) -> 1,
    Device.generateWithName("dehum")(Dehumidifiers.apply) -> 3,
    Device.generateWithName("heatactuator")(HeatActuator.apply) -> 3,
    Device.generateWithName("fanactuator")(FanActuator.apply) -> 3
  ))



  private def createSensors(sensors: List[(Stream[Sensor], Int)]): Unit = {
    for {
      deviceEntry <- sensors
      sensor <- deviceEntry._1.take(deviceEntry._2)
    } debugJoin(deviceClient join sensor)
  }

  private def createActuators(actuators: List[(Stream[Actuator], Int)]): Unit = {
    for {
      deviceEntry <- actuators
      actuator <- deviceEntry._1.take(deviceEntry._2)
    } debugJoin(deviceClient join actuator)
  }

  private def debugJoin(future: Future[String]): Unit = {
    future.onComplete {
      case Failure(exception) => println(exception)
      case Success(value) => println(s"Success join $value")
    }
  }
}
