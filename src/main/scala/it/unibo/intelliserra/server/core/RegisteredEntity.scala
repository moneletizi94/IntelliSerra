package it.unibo.intelliserra.server.core

import it.unibo.intelliserra.core.entity.{ActingCapability, Capability, SensingCapability}

sealed trait RegisteredEntity{
  def identifier : String
  def capabilities : Capability
}
case class RegisteredActuator(override val identifier: String, override val capabilities : ActingCapability) extends RegisteredEntity
case class RegisteredSensor(override val identifier: String, override val capabilities: SensingCapability) extends RegisteredEntity
