package it.unibo.intelliserra.core.entity

import it.unibo.intelliserra.core.sensor.Category

sealed trait Capability

final case class SensingCapability(category: Category) extends Capability
//TODO actingCapability