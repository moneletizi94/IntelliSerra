package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Protocol._
import akka.util.Timeout
import it.unibo.intelliserra.common.communication._
import it.unibo.intelliserra.core.actuator.{Action, OperationalState}
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.server.core.{RegisteredActuator, RegisteredEntity, RegisteredSensor}

import scala.concurrent.duration._
import akka.pattern.ask
import akka.pattern.pipe
import it.unibo.intelliserra.core.entity.{ActingCapability, Capability, SensingCapability}

import scala.concurrent.ExecutionContext

private[zone] class ZoneActor extends Actor with ActorLogging {

  //var List[Aggregator] = Aggregators
  var sensorsValue : Map[ActorRef, Measure] = Map()
  var associatedEntities : Map[ActorRef, Capability] = Map() 
  val actuatorsState : Map[ActorRef, OperationalState] = Map()

  implicit val timeout : Timeout = Timeout(5 seconds)
  implicit val ec: ExecutionContext = context.dispatcher
  // TODO: do BaseActor 

  override def receive: Receive = {
    case DestroyYourself =>
      associatedEntities.keySet.foreach(entity => entity ! DissociateFromMe(self))
      context stop self
    case AssignSensor(sensorRef, sensor: RegisteredEntity) =>
      sensorRef ? AssociateToMe(self) map { _ => GetEntityInfo(sender ,sensorRef, sensor)} pipeTo self
    case IsEntityAssociated(entityRef) =>
      associatedEntities.keySet.find(associatedEntity => associatedEntity == entityRef)
                                .map(entityRef => IsAssociated(entityRef)).getOrElse(IsNotAssociated)
    case DeAssignSensor(entityRef: ActorRef) => entityRef ! DissociateFromMe(self)
    case GetEntityInfo(replyTo , sensorRef, registeredSensor) =>
      associatedEntities += (sensorRef -> registeredSensor.capabilities)
      replyTo ! AssignOk
    case Tick =>
      /*sensorsValue.values.groupBy(measure => (measure.category, measure))
                          .map({case (category, measures) => state = (category, /*aggregators.aggregate(measures)*/measures)})*/
    case DoActions(actions) =>
      /*associatedActuators.map(actuator => (actuator._1,actuator._2.capabilities.actions.intersect(actions)))
                          .filter(_._2.nonEmpty)
                          //.flatMap()*/
  }


  private case class GetEntityInfo(replyTo : ActorRef, sensorRef : ActorRef, sensor: RegisteredEntity)
  private case object Tick
  private case class DoActions(actions : Set[Action])
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
