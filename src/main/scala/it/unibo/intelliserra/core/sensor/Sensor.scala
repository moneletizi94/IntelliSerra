package it.unibo.intelliserra.core.sensor

import it.unibo.intelliserra.core.Device
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.SensingCapability

import scala.concurrent.duration.FiniteDuration

trait Sensor extends Device {
  override def capability: SensingCapability
  def readPeriod: FiniteDuration
  def read(): Measure
}