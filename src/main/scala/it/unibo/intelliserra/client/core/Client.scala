package it.unibo.intelliserra.client.core

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, ActorSystem, Address, Props, Stash}
import it.unibo.intelliserra.common.communication.Protocol._

import scala.util.{Failure, Success}

sealed trait ServerResponse[+T]
case class SuccessResponse[T](content: T) extends ServerResponse[T]
case class FailResponse(ex: Exception) extends ServerResponse[Nothing]

private[core] object Client {

  /**
   * Create a client using akka actor
   * @param serverUri   the uri of server actor
   * @param actorSystem the actorSystem to be used for create the client actor
   * @return a new instance of client
   */
  def apply(serverUri: String)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf Props(new ClientImpl(serverUri))

  private[core] class ClientImpl(serverUri: String) extends Actor with Stash with ActorLogging {

    private val serverActor = context actorSelection serverUri

    private def handleRequest: Receive = {
      case CreateZone(zone) =>
        waitResponse(sender)
        serverActor ! CreateZone(zone)

      case RemoveZone(zone) =>
        waitResponse(sender)
        serverActor ! RemoveZone(zone)

      case GetZones =>
        waitResponse(sender)
        serverActor ! GetZones

      case msg => log.debug(s"ignored unknown request $msg")
    }

    // TODO: refactor using a common request-response protocol
    private def waitResponseBehaviour(replyTo: ActorRef): Receive = {
      case ZoneCreationError =>
        replyTo ! Failure(new IllegalStateException("fail during zone creation"))

      case NoZone =>
        replyTo ! Failure(new IllegalArgumentException("zone not found"))

      case ZoneCreated =>
        replyTo ! Success(ZoneCreated)

      case ZoneRemoved =>
        replyTo ! Success(ZoneRemoved)

      case Zones(zones) =>
        replyTo ! Success(zones)
    }

    override def receive: Receive = handleRequest

    private def waitResponse(replyTo: ActorRef): Unit = {
      context.become(waitResponseBehaviour(replyTo).andThen(_ => context.become(handleRequest)))
    }
  }
}