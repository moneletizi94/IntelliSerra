package it.unibo.intelliserra.device

import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor

import scala.concurrent.Future

trait DeviceDeploy {
  def deploySensor(sensor: Sensor): Future[Unit]
  def deployActuator(actuator: Actuator): Future[Unit]
}

