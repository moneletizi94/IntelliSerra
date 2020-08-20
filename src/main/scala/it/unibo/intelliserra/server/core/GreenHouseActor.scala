package it.unibo.intelliserra.server.core

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}

private[server] object GreenHouseActor {

  sealed trait ServerCommand
  case object Start extends ServerCommand

  sealed trait ServerResponse
  case object Started extends ServerResponse
  case class ServerError(throwable: Throwable) extends ServerResponse

  def apply(name: String)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem actorOf (Props[GreenHouseActor], name = name)
  }
}

private[server] class GreenHouseActor extends Actor {

  private def idle: Receive = {
    case Start =>
      // start other actors
      context.become(running)
      sender ! Started
  }

  private def running: Receive = {
    case Start => sender ! ServerError(new IllegalStateException("Server is already running"))
    case _ => println("Do something")
  }

  override def receive: Receive = idle
}
