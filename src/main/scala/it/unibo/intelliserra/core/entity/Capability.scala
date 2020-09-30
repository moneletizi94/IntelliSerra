package it.unibo.intelliserra.core.entity

import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.perception.{Category, ValueType}

/**
 * What an entity can do. It is used to
 * distinguish among an actuator and a sensor
 */
sealed trait Capability {
  /**
   * Informs whether an entity is able of doing something or not
   * @param capability, the asked capability
   * @return
   */
  def includes(capability: Capability): Boolean
}

object Capability {
  type ActionTag = Class[_ <: Action]

  /**
   * What a sensor is able to do
   * @param category, it is the type of what the sensor can sense
   */
  final case class SensingCapability(category: Category[ValueType]) extends Capability {
    override def includes(capability: Capability): Boolean = capability match {
      case SensingCapability(`category`) => true
      case _ => false
    }
  }

  /**
   * What an actuator is able to do
   * @param actions, these are the actions that the actuator can actuate
   */
  final case class ActingCapability(actions: Set[ActionTag]) extends Capability {
    override def includes(capability: Capability): Boolean = capability match {
      case ActingCapability(includedActions) => includedActions.forall(actions contains)
      case _ => false
    }
  }

  /**
   * Factory to create a [[it.unibo.intelliserra.core.entity.Capability.SensingCapability]]
   * @param category, category of the sensing capability
   * @return [[it.unibo.intelliserra.core.entity.Capability.SensingCapability]] created
   */
  def sensing(category: Category[ValueType]): SensingCapability = SensingCapability(category)

  /**
   * Factory to create an [[it.unibo.intelliserra.core.entity.Capability.ActingCapability]]
   * given at least an ActionTag
   * @param action, specified actionTag
   * @param actions, optional other actions
   * @return [[it.unibo.intelliserra.core.entity.Capability.ActingCapability]] created
   */
  def acting(action: ActionTag, actions: ActionTag*): ActingCapability = ActingCapability(actions :+ action toSet)

  /**
   * Factory to create an [[it.unibo.intelliserra.core.entity.Capability.ActingCapability]]
   * given a set of actionTag
   * @param actionsTag, specified ActionTag
   * @return [[it.unibo.intelliserra.core.entity.Capability.ActingCapability]] created
   */
  def acting(actionsTag: Set[ActionTag] = Set()): ActingCapability = ActingCapability(actionsTag)
}