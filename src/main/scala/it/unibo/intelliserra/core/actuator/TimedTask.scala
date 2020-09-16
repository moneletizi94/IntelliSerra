package it.unibo.intelliserra.core.actuator

import scala.concurrent.duration.FiniteDuration

trait TimedTask {
  def callback: () => Unit
  def delay: FiniteDuration
}

object TimedTask {
  import scala.concurrent.duration._

  def now(): TimedTask = TimedTask(0 millis)
  def apply(delay: FiniteDuration, callback: () => Unit = () => {}): TimedTask =
    TimedTaskImpl(() => callback(), delay)

  case class TimedTaskImpl(override val callback: () => Unit,
                           override val delay: FiniteDuration) extends TimedTask
}
