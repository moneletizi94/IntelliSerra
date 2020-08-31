package it.unibo.intelliserra.core.sensor

import it.unibo.intelliserra.core.entity.SensingCapability
import monix.reactive.Observable


//TODO check for refactory with Actuator
trait Sensor {
  def identifier: String
  def capability: SensingCapability
  def measures: Observable[Measure]
}