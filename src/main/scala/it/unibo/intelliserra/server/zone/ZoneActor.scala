package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Protocol._

private[zone] class ZoneActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case DestroyYourself =>
      associatedEntities().foreach(entity => entity ! DissociateFromMe(self))
      context stop self
  }

  def associatedEntities(): List[ActorRef] = List()
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
