package it.unibo.intelliserra.core.sensor

import it.unibo.intelliserra.core.entity.SensingCapability
//TODO check for refactory with Actuator
trait Sensor {
  def identifier: String
  def capability: SensingCapability
  def state: Measure
}
