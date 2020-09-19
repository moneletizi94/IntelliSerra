package it.unibo.intelliserrademo.customdevice

import it.unibo.intelliserra.core.actuator.{Action, Actuator}
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, ActionTag, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor, ValueType}

import scala.concurrent.duration.FiniteDuration

object SimulatedDevice {

  case class CustomSensor[T <: ValueType](override val identifier: String,
                                          override val readPeriod: FiniteDuration,
                                          category: Category[T])
                                         (simulatedValue: Stream[T]) extends Sensor with DefaultDeviceLog {

    private val valueIterator = simulatedValue.iterator
    override def capability: SensingCapability = Capability.sensing(category)
    override def read(): Option[Measure] = Option(Measure(category)(valueIterator.next()))
  }

  case class CustomActuator(override val identifier: String,
                            handledActions : Set[ActionTag],
                            override val actionHandler: ActionHandler) extends Actuator with DefaultDeviceLog {

    override val capability: ActingCapability = Capability.acting(handledActions)
  }
}
