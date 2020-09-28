package it.unibo.intelliserra.device.core.actuator

import scala.concurrent.duration.FiniteDuration

trait Operation {
  def complete: () => Unit
  def delay: FiniteDuration
}

object Operation {
  import scala.concurrent.duration._

  def completed(): Operation = completeAfter(0 millis)
  def completeAfter(delay: FiniteDuration, callback: () => Unit = () => {}): Operation =
    OperationImpl(callback, delay)

  case class OperationImpl(override val complete: () => Unit,
                           override val delay: FiniteDuration) extends Operation

}