package it.unibo.intelliserra.server.core

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import it.unibo.intelliserra.common.communication._
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}
import it.unibo.intelliserra.server.zone.ZoneManagerActor

import scala.concurrent.duration._

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

  private implicit val actorSystem = context.system
  private implicit val executionContext = context.dispatcher
  private implicit val timeout = Timeout(5 seconds)

  private var zoneManagerActor: ActorRef = _

  private def idle: Receive = {
    case Start =>
      zoneManagerActor = ZoneManagerActor()
      context.become(running orElse routeZoneHandling)
      sender ! Started
  }

  private def running: Receive = {
    case Start => sender ! ServerError(new IllegalStateException("Server is already running"))
  }

  def routeZoneHandling: Receive = {
    case CreateZone(zoneName) => zoneManagerActor ? CreateZone(zoneName) pipeTo sender()
  }

  override def receive: Receive = idle
}