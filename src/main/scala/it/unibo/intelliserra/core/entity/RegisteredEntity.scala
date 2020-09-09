package it.unibo.intelliserra.core.entity

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, SensingCapability}

sealed trait RegisteredEntity {
  def identifier : String
  def capabilities : Capability
}
case class RegisteredActuator(override val identifier: String, override val capabilities : ActingCapability) extends RegisteredEntity
case class RegisteredSensor(override val identifier: String, override val capabilities: SensingCapability) extends RegisteredEntity

case class EntityChannel(entity: RegisteredEntity, channel: ActorRef)