package it.unibo.intelliserra.device.core

import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.entity.Device
import it.unibo.intelliserra.core.perception.Measure

import scala.concurrent.duration.FiniteDuration

trait Sensor extends Device with DeviceCallback {
  override def capability: SensingCapability
  def readPeriod: FiniteDuration
  def read(): Option[Measure]
}
