package it.unibo.intelliserra.core

import it.unibo.intelliserra.server.core.RegisteredEntity

trait Zone {
  def name: String
  def entities: Set[RegisteredEntity]
}

object Zone {
  def apply(zoneName: String, entities: RegisteredEntity*): Zone = new ZoneImpl(zoneName, entities.toSet)
  def apply(zoneName: String, entities: Set[RegisteredEntity]): Zone = new ZoneImpl(zoneName, entities)

  private class ZoneImpl(override val name: String, override val entities: Set[RegisteredEntity]) extends Zone
}