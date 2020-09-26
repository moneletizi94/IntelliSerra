package it.unibo.intelliserrademo.customdevice

import it.unibo.intelliserra.core.entity.Device
import it.unibo.intelliserra.device.core.DeviceCallback

trait DefaultDeviceLog { this: Device with DeviceCallback => // TODO: refactor with log method.
  override def onInit(): Unit = println(s"LOG Device($identifier): onInit()")
  override def onAssociateZone(zoneName: String): Unit = println(s"LOG Device($identifier): onAssociateZone($zoneName)")
  override def onDissociateZone(zoneName: String): Unit = println(s"LOG Device($identifier): onDissociateZone($zoneName)")
}
