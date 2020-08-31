package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.aggregation.Aggregator

private[zone] class ZoneActor(private val aggregators: List[Aggregator]) extends Actor with ActorLogging {

  override def receive: Receive = {
    case DestroyYourself =>
      associatedEntities().foreach(entity => entity ! DissociateFromMe(self))
      context stop self
  }

  def associatedEntities(): List[ActorRef] = List()
}

object ZoneActor {
  def apply(name: String, aggregators : List[Aggregator])(implicit system: ActorSystem): ActorRef = {
    require(Aggregator.atMostOneCategory(aggregators),"only one aggregator must be assigned for each category")
    system actorOf (Props(new ZoneActor(aggregators)), name)
  }
}
