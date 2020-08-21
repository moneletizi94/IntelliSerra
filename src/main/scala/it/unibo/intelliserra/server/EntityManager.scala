package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.entity.{ActingCapability, Capability, SensingCapability}
import it.unibo.intelliserra.server.EntityManager.{RegisteredActuator, RegisteredEntity, RegisteredSensor}
import it.unibo.intelliserra.common.communication._

class EntityManagerActor extends Actor{

  private[server] var entities : Map[RegisteredEntity, ActorRef] = Map()

  override def receive: Receive = {
    case JoinSensor(identifier, capabilities, sensorRef) => {
      addEntityIfNotExists(RegisteredSensor(identifier, capabilities),sensorRef) match {
        case Some(newMap) => entities = newMap ; sender ! JoinOK
        case None => sender ! JoinError
      }
    }
    case JoinActuator(identifier, capabilities, sensorRef) => {
      addEntityIfNotExists(RegisteredActuator(identifier, capabilities),sensorRef) match {
        case Some(newMap) => entities = newMap ; sender ! JoinOK
        case None => sender ! JoinError
      }
    }
  }

  private def addEntityIfNotExists(registeredEntity: RegisteredEntity, actorRef: ActorRef) : Option[Map[RegisteredEntity, ActorRef]] = {
    entities.find(pair => pair._1.identifier == registeredEntity.identifier)
            .fold[Option[Map[RegisteredEntity, ActorRef]]](Some(entities + (registeredEntity -> actorRef)))(_ => None)
  }
}

object EntityManager{
  val name = "entityManager"

  def apply()(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props[EntityManagerActor], name)
  }

  sealed trait RegisteredEntity{
    val identifier : String
    val capabilities : Capability
  }
  case class RegisteredActuator(override val identifier: String, override val capabilities : ActingCapability) extends RegisteredEntity
  case class RegisteredSensor(override val identifier: String, override val capabilities: SensingCapability) extends RegisteredEntity

}
