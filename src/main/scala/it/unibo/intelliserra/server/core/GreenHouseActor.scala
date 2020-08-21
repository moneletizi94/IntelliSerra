package it.unibo.intelliserra.server.core

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}

private[core] object GreenHouseActor {

  sealed trait ServerCommand

  /**
   * Start the server
   */
  case object Start extends ServerCommand

  /**
   * Responses to server commands
   */
  sealed trait ServerResponse
  case object Started extends ServerResponse
  final case class ServerError(throwable: Throwable) extends ServerResponse

  /**
   * Create a green house server actor
   * @param name        the name of server actor
   * @param actorSystem the actor system for create the actor
   * @return an actor ref of green house server actor
   */
  def apply(name: String)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem actorOf (Props[GreenHouseActor], name = name)
  }
}

private[core] class GreenHouseActor extends Actor {

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
