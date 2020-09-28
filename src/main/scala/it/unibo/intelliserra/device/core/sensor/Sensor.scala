package it.unibo.intelliserra.device.core.sensor

import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.entity.{Capability, Device}
import it.unibo.intelliserra.core.perception.{Category, Measure, ValueType}

import scala.concurrent.duration.FiniteDuration

trait Sensor extends Device {
  /**
   *
   * @return
   */
  def sensePeriod: FiniteDuration

  /**
   *
   * @return
   */
  def sense(): Option[Measure] = Option(read()).filter(m => capability.includes(Capability.sensing(m.category)))

  /**
   *
   * @return
   */
  protected def read(): Measure
}

object Sensor {

  private class PollingSensorImpl(override val identifier: String,
                                  override val capability: SensingCapability,
                                  override val sensePeriod: FiniteDuration,
                                  measureStream: Stream[Measure]) extends Sensor {
    private val measureIterator = measureStream.iterator
    override def read(): Measure = measureIterator.next()
  }

  /**
   *
   * @param identifier
   * @param category
   * @param pollingPeriod
   * @param measureStream
   * @tparam V
   * @return
   */
  def apply[V <: ValueType](identifier: String,
                            category: Category[V],
                            pollingPeriod: FiniteDuration)
                           (measureStream: Stream[V]): Sensor =
    new PollingSensorImpl(identifier, Capability.sensing(category), pollingPeriod, measureStream.map(Measure(category)(_)))
}