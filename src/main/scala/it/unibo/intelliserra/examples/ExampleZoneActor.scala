package it.unibo.intelliserra.examples

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

import scala.concurrent.duration._
import it.unibo.intelliserra.examples.ExampleZoneActor.{AssignCompleted, AssignSensor, AssignToMe, DissociateFromMe, DissociateSensor}

class ExampleZoneActor extends Actor {

  private implicit val executionContext = context.dispatcher
  private implicit val timeout = Timeout(5 seconds)
  private[examples] var sensors: List[ActorRef] = List()

  override def receive: Receive = {
    case AssignSensor(sensor) => (sensor ? AssignToMe(self)).map(_ => AssignCompleted(sensor)).pipeTo(self)
    case DissociateSensor(sensor) => sensor ! DissociateFromMe(self)
    case AssignCompleted(sensor) => sensors = sensor :: sensors
    _
  }
}

object ExampleZoneActor {
  case class AssignSensor(actorRef: ActorRef)
  case class DissociateSensor(actorRef: ActorRef)
  case class AssignToMe(zoneRef: ActorRef)
  case class DissociateFromMe(zoneRef: ActorRef)
  case class AssignCompleted(sensor: ActorRef)
}