package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

class ZoneActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ => "fake"
  }
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
