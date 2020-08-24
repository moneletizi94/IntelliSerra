package it.unibo.intelliserra.client.core

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Address, Props, Stash}
import it.unibo.intelliserra.common.communication.Protocol._

import scala.util.{Failure, Success}

private[core] object Client {

  /**
   * Create a client using akka actor
   * @param serverUri   the uri of server actor
   * @param actorSystem the actorSystem to be used for create the client actor
   * @return a new instance of client
   */
  def apply(serverUri: String)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf Props(new ClientImpl(serverUri))

  private[core] class ClientImpl(serverUri: String) extends Actor with Stash {

    private val serverActor = context actorSelection serverUri

    private def handleRequest: Receive = {
      case CreateZone(zone) =>
        context.become(waitResponse(sender))
        serverActor ! CreateZone(zone)

      case RemoveZone(zone) =>
        context.become(waitResponse(sender))
        serverActor ! RemoveZone(zone)
    }

    private def waitResponse(replyTo: ActorRef): Receive = {
      case ZoneCreationError =>
        context.become(handleRequest)
        replyTo ! Failure(new IllegalStateException("fail during zone creation"))

      case NoZone =>
        context.become(handleRequest)
        replyTo ! Failure(new IllegalArgumentException("zone not found"))

      case ZoneCreated =>
        context.become(handleRequest)
        replyTo ! Success(ZoneCreated)

      case ZoneRemoved =>
        context.become(handleRequest)
        replyTo ! Success(ZoneRemoved)
    }

    override def receive: Receive = handleRequest
  }
}