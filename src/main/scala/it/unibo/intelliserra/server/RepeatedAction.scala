package it.unibo.intelliserra.server

import akka.actor.{Actor, Timers}

import scala.concurrent.duration._

trait RepeatedAction[M <: Any] extends Timers { this : Actor =>

  // TODO: scaladoc 
  val repeatedActionRate : FiniteDuration
  val repeatedMessage : M

  override def preStart(): Unit = {
    timers.startTimerAtFixedRate("key", repeatedMessage, repeatedActionRate)
  }
}



