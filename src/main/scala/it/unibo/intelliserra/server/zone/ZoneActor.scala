package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages.{AddEntity, DeleteEntity, DoActions}
import it.unibo.intelliserra.core.actuator.OperationalState
import it.unibo.intelliserra.core.entity.EntityChannel
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.server.aggregation.Aggregator

private[zone] class ZoneActor(private val aggregators: List[Aggregator]) extends Actor with ActorLogging {

  private[zone] var sensorsValue: Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities: Set[EntityChannel] = Set()
  private[zone] var actuatorsState: Map[ActorRef, OperationalState] = Map()

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

}

object ZoneActor {
  def apply(name: String, aggregators: List[Aggregator])(implicit system: ActorSystem): ActorRef = {
    require(Aggregator.atMostOneCategory(aggregators), "only one aggregator must be assigned for each category")
    system actorOf(Props(new ZoneActor(aggregators)), name)
  }
}
