package it.unibo.intelliserra.core.entity

trait Entity {
  def identifier: String
  def capability: Capability
}
