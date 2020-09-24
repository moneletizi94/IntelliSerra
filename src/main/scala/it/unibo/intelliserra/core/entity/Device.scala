package it.unibo.intelliserra.core.entity

import akka.actor.ActorRef
import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, SensingCapability}

trait Device {
  def identifier : String
  def capability : Capability
}