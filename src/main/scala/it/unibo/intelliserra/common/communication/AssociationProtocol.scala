package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.server.core.RegisteredEntity

trait AssociationProtocol {
  /* --- From Zone to Sensor/ Actuator --- */
  case class DissociateFromMe(zoneRef: ActorRef)

  /* --- From Zone to Sensor/ Actuator --- */
  case class AssociateToMe(zoneRef: ActorRef)

  /* --- From GH to Zone --- */
  case class AssignSensor(actorRef: ActorRef, registeredEntity: RegisteredEntity)
  case class DeAssignSensor(actorRef: ActorRef)

  /* --- From GH to Zone --- */
  case class AssignActuator(actorRef: ActorRef, registeredEntity: RegisteredEntity)
  case class DeAssignActuator(actorRef: ActorRef)


}
