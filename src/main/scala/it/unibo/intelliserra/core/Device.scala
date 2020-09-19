package it.unibo.intelliserra.core

import it.unibo.intelliserra.core.entity.Capability

trait Device {
  /**
   * The unique identifier associated to a Device
   */
  def identifier: String

  /**
   * The capability of device [[Capability]]
   */
  def capability: Capability

  def onInit(): Unit

  def onAssociateZone(zoneName: String): Unit

  def onDissociateZone(zoneName: String): Unit
}

object Device {
  def generateWithName[T <: Device](seedName: String)(factory: String => T): Stream[T] =
    Stream.from(0).map(counter => factory(s"$seedName$counter"))
}