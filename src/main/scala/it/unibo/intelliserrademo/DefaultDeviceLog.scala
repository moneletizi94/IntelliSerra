package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.Device

trait DefaultDeviceLog extends Device { // TODO: refactor with log method.
  override def onInit(): Unit = println(s"LOG Device($identifier): onInit()")
  override def onAssociateZone(zoneName: String): Unit = println(s"LOG Device($identifier): onAssociateZone($zoneName)")
  override def onDissociateZone(zoneName: String): Unit = println(s"LOG Device($identifier): onDissociateZone($zoneName)")
}
