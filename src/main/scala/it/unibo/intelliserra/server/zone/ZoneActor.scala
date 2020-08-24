package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Protocol._
import akka.util.Timeout
import it.unibo.intelliserra.common.communication._
import it.unibo.intelliserra.core.actuator.{Action, OperationalState}
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.server.core.{RegisteredActuator, RegisteredSensor}

import scala.concurrent.duration._
import akka.pattern.ask
import akka.pattern.pipe

import scala.concurrent.ExecutionContext

private[zone] class ZoneActor extends Actor with ActorLogging {

  //var List[Aggregator] = Aggregators.
  // sensori: attore, ultimaMisura (la misura contiene la categoria)
  // devo sapere le azioni che sanno fare gli attuatori (tramite registered entity)
  // devo sapere cosa stanno facendo ora gli attuatori

  //potrei voler distinguere quelli che sono registrati da quelli che mi hanno mandato il valore


  //sbagliato perché dovrei andare a "ricreare"/modificare la coppia ad ogni ricezione di una misura
  /*ho bisogno della sensingCapability perchè se memorizzassi solo la misura potrei perdermi quelli che magari in quel tick non mi hanno mandato il valore
    in quel caso se mi chiedessero di disassociare dal client un sensore io potrei dire che non c'è invece è associato a me
  */
  //val sensorValue : Map[ActorRef, (Option[Measure], SensingCapability)] = Map()

  var sensorsValue : Map[ActorRef, Measure] = Map()
  var associatedSensors : Map[ActorRef, RegisteredSensor] = Map() //sensingCapabiity non mi serve ma lo metto per simmetria al momento
  val associatedActuators : Map[ActorRef, RegisteredActuator] = Map() //forse avrebbe più senso il contrario perchè mi capiterà molto più spesso di voler sapere se ho un'azione da far scattare sugli attuatori
  val actuatorsState : Map[ActorRef, OperationalState] = Map()

  implicit val timeout : Timeout = Timeout(5 seconds)
  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case DestroyYourself =>
      associatedEntities().foreach(entity => entity ! DissociateFromMe(self))
      context stop self
    case AssignSensor(sensorRef, sensor: RegisteredSensor) =>
      sensorRef ? AssociateToMe(self) map {_ => GetStructure(sensorRef, sensor)} pipeTo self
    case DeAssignSensor(actorRef: ActorRef) =>
      // TODO: dare errore se mi chiede di dissociare un sensore che non è associato a questa zona?
      associatedSensors.find(refAndCapabilities => refAndCapabilities._1 == actorRef)
                        .map(refAndCapabilities => refAndCapabilities._1 ! DissociateFromMe(self))
    case GetStructure(sensorRef, registeredSensor) =>
      associatedSensors += (sensorRef -> registeredSensor)
    case Tick =>
      sensorsValue.values.groupBy(measure => measure.category)
                          .map({case (category, measures) => (category, /*aggregators.aggregate(measures)*/measures)})
    case DoActions(actions) =>
      associatedActuators.map(actuator => (actuator._1,actuator._2.capabilities.actions.intersect(actions)))
                          .filter(_._2.nonEmpty)
                          //.flatMap()



  }

  def associatedEntities(): Set[ActorRef] = associatedSensors.keySet union associatedActuators.keySet

  private case class GetStructure(sensorRef : ActorRef, sensor: RegisteredSensor)
  private case object Tick
  private case class DoActions(actions : Set[Action])
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
