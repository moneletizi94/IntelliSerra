package it.unibo.intelliserra.client.core

import scala.concurrent.Future

trait ZoneClient {
  type Zone
  def createZone(zone: Zone): Future[Zone]
  def removeZone(zone: Zone): Future[Zone]
}