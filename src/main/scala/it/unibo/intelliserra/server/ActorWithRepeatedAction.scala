package it.unibo.intelliserra.server

import akka.actor.{Actor, Timers}

import scala.concurrent.duration._

trait ActorWithRepeatedAction[M <: Any] extends Actor with Timers{

  val rate : FiniteDuration
  val repeatedMessage : M

  override def preStart(): Unit = {
    timers.startTimerAtFixedRate("key", repeatedMessage, rate)
  }
}



