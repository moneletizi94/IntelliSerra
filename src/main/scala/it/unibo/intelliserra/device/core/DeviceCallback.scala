package it.unibo.intelliserra.device.core

import it.unibo.intelliserra.core.entity.Device

trait DeviceCallback { this: Device =>
  def onInit(): Unit
  def onAssociateZone(zoneName: String): Unit
  def onDissociateZone(zoneName: String): Unit
}
