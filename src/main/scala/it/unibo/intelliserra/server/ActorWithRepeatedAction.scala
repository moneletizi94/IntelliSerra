package it.unibo.intelliserra.server

editimport akka.actor.{Actor, Timers}
import it.unibo.intelliserra.core.sensor.StringType
import it.unibo.intelliserra.server.ActorWithRepeatedAction.Tick

import scala.concurrent.duration._
import scala.util.Random

trait ActorWithRepeatedAction extends Actor with Timers{

  val rate : FiniteDuration

  override def preStart(): Unit = {
    timers.startTimerAtFixedRate("key",Tick, rate)
  }
}

// TODO: visibility? 
object ActorWithRepeatedAction{
  case object Tick
}
