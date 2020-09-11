package it.unibo.intelliserra.examples

import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.sensor.{Measure, Sensor}

import scala.concurrent.duration._

object SensorActuatorExample extends App {

  object ExampleSensors {

    def fromStream(identifier: String, capability: SensingCapability, readPeriod: FiniteDuration)
                  (measures: Stream[Measure]): Sensor =
      StreamSensor(identifier, capability, readPeriod, measures)

    private case class StreamSensor(override val identifier: String,
                                    override val capability: SensingCapability,
                                    override val readPeriod: FiniteDuration,
                                    measures: Stream[Measure]) extends Sensor {
      override def read(): Option[Measure] = Option(measures.iterator.next())
      override def onInit(): Unit = ()
      override def onAssociateZone(zoneName: String): Unit = ()
      override def onDissociateZone(zoneName: String): Unit = ()
    }
  }

}
