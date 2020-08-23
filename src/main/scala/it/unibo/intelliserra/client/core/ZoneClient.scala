package it.unibo.intelliserra.client.core

import scala.concurrent.Future

trait ZoneClient {
  type Zone = String

  /**
   * Create a new zone with specified name
   * @param zone  the name of zone
   * @return if success, the name of zone, otherwise a failure
   */
  def createZone(zone: Zone): Future[Zone]


  /**
   * Remove an existing zone with specified name
   * @param zone  the name of zone
   * @return if success, the name of zone, otherwise a failure
   */
  def removeZone(zone: Zone): Future[Zone]
}