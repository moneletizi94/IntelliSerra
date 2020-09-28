package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.SensorMeasureUpdated
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.sensor.SensorActor.SensorPollingTime


// It's lazy factory for sensor
class SensorActor(sensor: Sensor) extends DeviceActor with Timers with ActorLogging {

  override def receive: Receive = handleZoneManagement(None)

  override protected def onAssociated(zoneRef: ActorRef, zoneName: String): Unit = {
    timers.startTimerAtFixedRate(sensor.identifier, SensorPollingTime, sensor.sensePeriod)
  }

  override protected def onDissociate(zoneRef: ActorRef, zoneName: String): Unit = {
    timers.cancel(sensor.identifier)
  }

  override protected def associatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive = {
    case SensorPollingTime => sensor.sense().foreach(zoneRef ! SensorMeasureUpdated(_))
  }

  override protected def dissociatedBehaviour(zoneRef: ActorRef, zoneName: String): Receive = {
    case SensorPollingTime => log.info("ignoring polling when dissociated")
  }
}

object SensorActor {
  private case object SensorPollingTime
  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(sensor)
  def props(sensor: Sensor): Props = Props(new SensorActor(sensor))
}
