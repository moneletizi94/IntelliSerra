package it.unibo.intelliserra.server.entityManager

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.server.entityManager.EMEventBus.PublishedOnRemoveEntity

private[server] class EntityManagerActor() extends Actor
  with ActorLogging {

  private[server] var entities : List[DeviceChannel] = List()

  override def receive: Receive = {

    case JoinDevice(identifier, capabilities, deviceRef) =>
      addEntityAndSendResponse(RegisteredDevice(identifier, capabilities), deviceRef, sender)

    case GetEntity(identifier) =>
      sender ! entities.find(e => e.device.identifier == identifier).map(e => EntityResult(e)).getOrElse(EntityNotFound)

    case RemoveEntity(identifier) =>
      sender ! entities.find(e => e.device.identifier == identifier)
        .fold[EntityManagerResponse](EntityNotFound)(entityChannel => {
          entities = entities.filter(_ != entityChannel)
          EMEventBus.publish(EMEventBus.topic, PublishedOnRemoveEntity(entityChannel))
          EntityRemoved
        })

  }

  private def addEntityIfNotExists(registeredEntity: RegisteredDevice, actorRef: ActorRef) : Option[List[DeviceChannel]] = {
    entities.find(elem => elem.device.identifier == registeredEntity.identifier)
            .fold(Option(DeviceChannel(registeredEntity, actorRef) :: entities))(_ => None)
  }

  private def addEntityAndSendResponse(registeredEntity: RegisteredDevice, entityRef : ActorRef, replyTo: ActorRef): Unit = {
      replyTo ! addEntityIfNotExists(registeredEntity,entityRef)
                                  .map(newMap => {entities = newMap; JoinOK})
                                  .getOrElse(JoinError("identifier already exists"))
  }

}

object EntityManagerActor {
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
