package it.unibo.intelliserra.device.core.sensor

import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.entity.{Capability, Device}
import it.unibo.intelliserra.core.perception.{Category, Measure, ValueType}

import scala.concurrent.duration.FiniteDuration

/**
 * Rich interface that represent a sensor.
 * It allow to define how to read a measure and the interval between a measure and the next
 */
trait Sensor extends Device {

  /** The desired interval between sense() call and the next. */
  def sensePeriod: FiniteDuration

  /**
   * Produce a measure reading from sensor accordingly to his capability.
   * @return a measure perceived by the sensor, an empty measure if not
   */
  def sense(): Option[Measure] = Option(read()).filter(m => capability.includes(Capability.sensing(m.category)))

  /**
   * Called when sense() template method is called.
   * @return the measure perceived
   */
  protected def read(): Measure
}

object Sensor {

  /**
   * Factory for create a simple sensor starting from a stream of measure.
   * @param identifier      the name of sensor
   * @param category        the category of measure produced by sensor
   * @param sensePeriod     the production rate of sensor
   * @param measureStream   the stream of measures
   * @tparam V              the type of wrapped primitive type. See [[ValueType]]
   * @return the sensor created
   */
  def apply[V <: ValueType](identifier: String,
                            category: Category[V],
                            sensePeriod: FiniteDuration)
                           (measureStream: Stream[V]): Sensor =
    new DefaultSensorImpl(identifier, Capability.sensing(category), sensePeriod, measureStream.map(Measure(category)(_)))

  private class DefaultSensorImpl(override val identifier: String,
                                  override val capability: SensingCapability,
                                  override val sensePeriod: FiniteDuration,
                                  measureStream: Stream[Measure]) extends Sensor {
    private val measureIterator = measureStream.iterator
    override def read(): Measure = measureIterator.next()
  }
}