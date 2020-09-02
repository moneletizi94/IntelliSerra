package it.unibo.intelliserra.core.entity

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{Category, ValueType}

sealed trait Capability

final case class SensingCapability(category: Category[ValueType]) extends Capability
final case class ActingCapability(actions: Set[Action]) extends Capability