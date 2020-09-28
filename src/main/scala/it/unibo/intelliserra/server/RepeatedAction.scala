package it.unibo.intelliserra.server

import akka.actor.{Actor, Timers}

import scala.concurrent.duration._

/**
 * It demand to the classes that extend it the definition of the periodic message they intend to receive and the scheduling rate
 * @tparam M the periodic message that you want to manage
 */
trait RepeatedAction[M <: Any] extends Timers { this : Actor =>

  /** the periodic rate of message reception */
  val repeatedActionRate : FiniteDuration

  /** the periodic message that you want to receive */
  val repeatedMessage : M

  override def preStart(): Unit = {
    timers.startTimerAtFixedRate("key", repeatedMessage, repeatedActionRate)
  }
}



