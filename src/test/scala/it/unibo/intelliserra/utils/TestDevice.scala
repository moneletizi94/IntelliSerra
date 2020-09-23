package it.unibo.intelliserra.utils

import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.perception.Measure
import it.unibo.intelliserra.device.core.Actuator.ActionHandler
import it.unibo.intelliserra.device.core.{Actuator, Sensor}

import scala.concurrent.duration.FiniteDuration

object TestDevice {

  case class TestActuator(override val identifier: String,
                          override val capability: ActingCapability)
                         (override val actionHandler: ActionHandler) extends Actuator {
    override def onInit(): Unit = {}
    override def onAssociateZone(zoneName: String): Unit = {}
    override def onDissociateZone(zoneName: String): Unit = {}
  }

  case class TestSensor(override val identifier: String,
                        override val capability: SensingCapability,
                        override val readPeriod: FiniteDuration,
                        measures: Stream[Measure]) extends Sensor {

    override def read(): Option[Measure] = Option(measures.iterator.next())
    override def onInit(): Unit = {}
    override def onAssociateZone(zoneName: String): Unit = {}
    override def onDissociateZone(zoneName: String): Unit = {}
  }

}
