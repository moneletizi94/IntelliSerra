package it.unibo.intelliserra.core.entity

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{Category, ValueType}

sealed trait Capability

object Capability {
  final case class SensingCapability(category: Category[ValueType]) extends Capability
  final case class ActingCapability(actions: Set[Action]) extends Capability

  def sensing(category: Category[ValueType]): SensingCapability = SensingCapability(category)
  def acting(actions: Set[Action]): ActingCapability = ActingCapability(actions)
  def acting(action: Action, actions: Action*): ActingCapability = ActingCapability(actions :+ action toSet)

  def canDo(capability: Capability, action: Action): Boolean = capability match {
    case ActingCapability(actions) => actions.contains(action)
    case _ => false
  }

  def canSense(capability: Capability, category: Category[ValueType]): Boolean = capability match {
    case SensingCapability(`category`) => true
    case _ => false
  }
}