package it.unibo.intelliserra.core.entity

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.Category

sealed trait Capability

final case class SensingCapability(category: Category) extends Capability
final case class ActingCapability(actions: Set[Action]) extends Capability