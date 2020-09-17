package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.SensorMeasureUpdated
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.sensor.SensorActor.SensorPollingTime

class SensorActor(override val device: Sensor) extends DeviceActor with Timers with ActorLogging {

  override def receive: Receive = zoneManagement orElse fallback

  override protected def associateBehaviour(zoneRef: ActorRef): Receive = {
    timers.startTimerAtFixedRate(device.identifier, SensorPollingTime, device.readPeriod)
    val newBehaviour: PartialFunction[Any, Unit] = {
      case SensorPollingTime =>
        device.read().filter(_.category == device.capability.category).foreach {
          measure =>
            log.info(s"Sending measure: $measure")
            zoneRef ! SensorMeasureUpdated(measure)
        }
    }
    newBehaviour
  }

  override protected def dissociateBehaviour(zoneRef: ActorRef): Receive = {
    case SensorPollingTime =>
      log.debug(s"${device.identifier} not associated: ignoring polling period")
  }
}

object SensorActor {
  private case object SensorPollingTime

  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(sensor)
  def props(sensor: Sensor): Props = Props(new SensorActor(sensor))
}
