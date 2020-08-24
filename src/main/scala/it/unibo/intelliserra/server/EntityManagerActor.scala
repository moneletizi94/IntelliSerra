package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.core.{RegisteredActuator, RegisteredEntity, RegisteredSensor}

private[server] class EntityManagerActor extends Actor{

  private[server] var entities : Map[RegisteredEntity, ActorRef] = Map()

  override def receive: Receive = {
    case JoinSensor(identifier, capabilities, sensorRef) =>
      addEntityAndSendResponse(RegisteredSensor(identifier, capabilities), sensorRef, sender)

    case JoinActuator(identifier, capabilities, actuatorRef) =>
      addEntityAndSendResponse(RegisteredActuator(identifier, capabilities), actuatorRef, sender)

    case EntityExists(identifier) =>
      sender ! entities.find(p => p._1.identifier == identifier).map(p => Entity(p._1, p._2))
  }

  private def addEntityIfNotExists(registeredEntity: RegisteredEntity, actorRef: ActorRef) : Option[Map[RegisteredEntity, ActorRef]] = {
    entities.find(pair => pair._1.identifier == registeredEntity.identifier)
            .fold(Option(entities + (registeredEntity -> actorRef)))(_ => None)
  }

  private def addEntityAndSendResponse(registeredEntity: RegisteredEntity, entityRef : ActorRef, replyTo: ActorRef): Unit = {
      replyTo ! addEntityIfNotExists(registeredEntity,entityRef)
                                  .map(newMap => {entities = newMap; JoinOK})
                                  .getOrElse(JoinError("identifier already exists"))
  }

}

object EntityManagerActor{
  val name = "entityManager"

  /**
   * Create an entity manager actor
   * @param actorSystem the actor system for create the actor
   * @return an actor ref of entity manager actor
   */
  def apply()(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props[EntityManagerActor], name)
  }

}
