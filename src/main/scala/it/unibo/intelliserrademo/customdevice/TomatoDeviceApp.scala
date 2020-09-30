package it.unibo.intelliserrademo.customdevice

import akka.dispatch.ExecutionContexts
import it.unibo.intelliserra.core.entity.Device
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.device.core.actuator.Actuator
import it.unibo.intelliserra.device.core.sensor.Sensor
import it.unibo.intelliserrademo.common.DefaultAppConfig
import it.unibo.intelliserrademo.customdevice.TomatoActuators.{Dehumidifiers, FanActuator, HeatActuator, WaterActuator}
import it.unibo.intelliserrademo.customdevice.TomatoSensors._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
object TomatoDeviceApp extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContexts.global()

  val deviceClient = DeviceDeploy(DefaultAppConfig.GreenhouseName, DefaultAppConfig.Hostname, DefaultAppConfig.Port)

  createSensors(List(
    generateDeviceWithName("weather")(WeatherSensor.apply) -> 10,
    generateDeviceWithName("airtemp")(AirTemperatureSensor.apply) -> 10,
    generateDeviceWithName("airhum")(AirHumiditySensor.apply) -> 10,
    generateDeviceWithName("soilmoisture")(SoilMoistureSensor.apply) -> 5,
    generateDeviceWithName("daynight")(DayNightSensor.apply) -> 5
  ))

  createActuators(List(
    generateDeviceWithName("wateractuator")(WaterActuator.apply) -> 1,
    generateDeviceWithName("dehum")(Dehumidifiers.apply) -> 3,
    generateDeviceWithName("heatactuator")(HeatActuator.apply) -> 3,
    generateDeviceWithName("fanactuator")(FanActuator.apply) -> 3
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

  private def generateDeviceWithName[D <: Device](seedName: String)(deviceFactory: String => D): Stream[D] = {
    Stream.from(0).map(counter => deviceFactory(s"$seedName$counter"))
  }
}
