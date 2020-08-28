package it.unibo.intelliserra.core.sensor

import java.util.concurrent.Executors

import it.unibo.intelliserra.core.entity.SensingCapability
import it.unibo.intelliserra.core.sensor.MeasureProducer.PushProducer
import it.unibo.intelliserra.core.sensor.Sensor.BasicSensor
import monix.execution.Scheduler
import monix.reactive.Observable
import monix.reactive.subjects.PublishSubject

import scala.concurrent.duration.FiniteDuration


//TODO check for refactory with Actuator
trait Sensor {
  def identifier: String
  def capability: SensingCapability
  def measures: Observable[Measure]
}

object Sensor {

  def apply(identifier: String, capability: SensingCapability)(behaviour: MeasureProducer): Sensor =
    new SensorWithBehaviour(identifier, capability, behaviour)

  private[sensor] class SensorWithBehaviour(override val identifier: String,
                                            override val capability: SensingCapability,
                                            override val behaviour: MeasureProducer) extends BasicSensor

  abstract class BasicSensor extends Sensor {
    override def measures: Observable[Measure] = behaviour.measures//.filter(m => m.category == capability.category)
    protected def behaviour: MeasureProducer
  }

}

object Example extends App {

  case object Temperature extends Category
}