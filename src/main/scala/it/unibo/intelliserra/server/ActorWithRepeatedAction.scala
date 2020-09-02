package it.unibo.intelliserra.server

import akka.actor.Actor

import scala.concurrent.duration._

trait ActorWithRepeatedAction extends Actor{

  val rate : FiniteDuration

  protected case object Tick

  private val scheduler = context.system.scheduler

  override def preStart(): Unit = {
    scheduler.scheduleAtFixedRate(100 milliseconds, rate, self, Tick)(context.dispatcher)
  }

}
