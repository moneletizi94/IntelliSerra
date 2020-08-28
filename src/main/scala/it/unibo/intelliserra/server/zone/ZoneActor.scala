package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

private[zone] class ZoneActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ => "So Fake"
  }
/*
  //var List[Aggregator] = Aggregators
  private[zone] var sensorsValue : Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities : Set[EntityChannel] = Set()
  private[zone] var actuatorsState : Map[ActorRef, OperationalState] = Map()

  override def receive: Receive = {
    case AddEntity(entityChannel) => associatedEntities += entityChannel
    case DeleteEntity(entityChannel) => associatedEntities -= entityChannel
    case Tick =>
      /*sensorsValue.values.groupBy(measure => (measure.category, measure))
                          .map({case (category, measures) => state = (category, /*aggregators.aggregate(measures)*/measures)})*/
    case DoActions(actions) =>
      /*associatedActuators.map(actuator => (actuator._1,actuator._2.capabilities.actions.intersect(actions)))
                          .filter(_._2.nonEmpty)
                          //.flatMap()*/
  }


  private case object Tick
  private case class DoActions(actions : Set[Action])*/
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
