package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.core.sensor.{Measure, Sensor}
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.sensor.SensorActor.{SensorMeasureUpdated, SensorPollingTime}

class SensorActor(override val device: Sensor) extends DeviceActor with Timers with ActorLogging {

  override def receive: Receive = zoneManagement orElse fallback

  timers.startTimerAtFixedRate(device.identifier, SensorPollingTime, device.readPeriod)

  override protected def associateBehaviour(zoneRef: ActorRef): Receive = {
    case SensorPollingTime =>
      device.read().foreach(measure => zoneRef ! SensorMeasureUpdated(measure))
  }

  override protected def dissociateBehaviour(zoneRef: ActorRef): Receive = {
    case SensorPollingTime =>
      log.debug(s"${device.identifier} not associated: ignoring polling period")
  }
}

object SensorActor {
  case class SensorMeasureUpdated(measure: Measure) // TODO: merge with right message

  private case object SensorPollingTime

  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(sensor)
  def props(sensor: Sensor): Props = Props(new SensorActor(sensor))
}
