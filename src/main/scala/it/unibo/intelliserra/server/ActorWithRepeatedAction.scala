package it.unibo.intelliserra.server

import akka.actor.Actor

import scala.concurrent.duration._

trait ActorWithRepeatedAction extends Actor{

  def onTick(): Unit
  def rate : FiniteDuration

  private case object Tick

  private val scheduler = context.system.scheduler

  override def preStart(): Unit = {
    scheduler.scheduleAtFixedRate(100 milliseconds, rate, self, Tick)(context.dispatcher)
  }

  override def receive: Receive = {
    case Tick => onTick()
  }
}
