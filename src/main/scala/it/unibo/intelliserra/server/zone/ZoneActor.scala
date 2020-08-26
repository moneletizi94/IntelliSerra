package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import it.unibo.intelliserra.common.communication.Messages.{Ack, AddEntity, AssociateToMe, DeleteEntity, DestroyYourself, DissociateFromMe}
import it.unibo.intelliserra.core.actuator.{Action, OperationalState}
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.server.core.RegisteredEntity

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

private[zone] class ZoneActor extends Actor with ActorLogging {

  //var List[Aggregator] = Aggregators
  private[zone] var sensorsValue : Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities : Map[ActorRef, RegisteredEntity] = Map()
  private[zone] var actuatorsState : Map[ActorRef, OperationalState] = Map()

  override def receive: Receive = {
    case DestroyYourself =>
      associatedEntities.keySet.foreach(entity => entity ! DissociateFromMe(self))
      context stop self
    case AddEntity(entity, entityRef) => associatedEntities += (entityRef -> entity.capabilities)
    case DeleteEntity(entity, entityRef: ActorRef) => associatedEntities -= entityRef // TODO: entity is required?
    case Tick =>
      /*sensorsValue.values.groupBy(measure => (measure.category, measure))
                          .map({case (category, measures) => state = (category, /*aggregators.aggregate(measures)*/measures)})*/
    case DoActions(actions) =>
      /*associatedActuators.map(actuator => (actuator._1,actuator._2.capabilities.actions.intersect(actions)))
                          .filter(_._2.nonEmpty)
                          //.flatMap()*/
  }

  private case object Tick
  private case class DoActions(actions : Set[Action])
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
