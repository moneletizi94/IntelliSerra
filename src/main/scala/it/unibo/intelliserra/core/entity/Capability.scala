package it.unibo.intelliserra.core.entity

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{Category, ValueType}

sealed trait Capability {
  def includes(capability: Capability): Boolean
}

object Capability {
  type ActionTag = Class[_ <: Action]

  final case class SensingCapability(category: Category[ValueType]) extends Capability {
    override def includes(capability: Capability): Boolean = capability match {
      case SensingCapability(`category`) => true
      case _ => false
    }
  }

  final case class ActingCapability(actions: Set[ActionTag]) extends Capability {
    override def includes(capability: Capability): Boolean = capability match {
      case ActingCapability(includedActions) => includedActions.forall(actions contains)
      case _ => false
    }
  }

  def sensing(category: Category[ValueType]): SensingCapability = SensingCapability(category)
  def acting(action: ActionTag, actions: ActionTag*): ActingCapability = ActingCapability(actions :+ action toSet)
  def acting(actions: Set[ActionTag] = Set()): ActingCapability = ActingCapability(actions)
}