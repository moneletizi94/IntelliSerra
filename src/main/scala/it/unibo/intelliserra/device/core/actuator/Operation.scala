package it.unibo.intelliserra.device.core.actuator

import scala.concurrent.duration.FiniteDuration

/**
 * Represent an operation to physical world that can be completed immediately or deferred.
 */
trait Operation {

  /** Mark the action as complete and invoke the associated callback */
  def complete: () => Unit

  /** Timeout that when it expires the operation is mark as completed */
  def timeout: FiniteDuration
}

object Operation {
  import scala.concurrent.duration._

  /** Create an operation completed */
  def completed(): Operation = completeAfter(0 millis)

  /***
   * Create an operation to be completed in deferred mode.
   * @param timeout     the timeout
   * @param callback    the callback called when operation is completed.
   * @return  an operation deferred
   */
  def completeAfter(timeout: FiniteDuration, callback: () => Unit = () => {}): Operation =
    OperationImpl(callback, timeout)

  private case class OperationImpl(override val complete: () => Unit,
                                   override val timeout: FiniteDuration) extends Operation

}