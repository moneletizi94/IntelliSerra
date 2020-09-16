package it.unibo.intelliserrademo.customsensor

import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor, ValueType}
import it.unibo.intelliserrademo.DefaultDeviceLog

import scala.concurrent.duration.FiniteDuration

object SimulatedSensor {

  case class CustomSensor[T <: ValueType](override val identifier: String,
                                          override val readPeriod: FiniteDuration,
                                          category: Category[T])
                                         (simulatedValue: Stream[T]) extends Sensor with DefaultDeviceLog {

    private val valueIterator = simulatedValue.iterator
    override def capability: SensingCapability = Capability.sensing(category)
    override def read(): Option[Measure] = Option(Measure(category)(valueIterator.next()))
  }
}
