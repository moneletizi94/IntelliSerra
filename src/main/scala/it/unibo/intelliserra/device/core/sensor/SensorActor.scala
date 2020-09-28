package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.SensorMeasureUpdated
import it.unibo.intelliserra.device.core.DeviceActor
import it.unibo.intelliserra.device.core.sensor.SensorActor.SensorPollingTime

private[device] object SensorActor {
  // self send message when is time to read value from sensor
  private case object SensorPollingTime

  /**
   * Create a SensorActor from Sensor.
   * The actor act as controller of sensor.
   * @param sensor        the sensor to be controlled by
   * @param actorSystem   the actor system for create the actor
   * @return an actor ref
   */
  def apply(sensor: Sensor)(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf props(sensor)

  /**
   * Create a props configuration for instantiate a SensorActor.
   * The actor act as controller of sensor.
   * @param sensor  the sensor to be controlled by
   * @return the configuration object for instantiate an actor
   */
  def props(sensor: Sensor): Props = Props(new SensorActor(sensor))
}

private[device] class SensorActor(sensor: Sensor) extends DeviceActor with Timers with ActorLogging {

  override def receive: Receive = zoneManagementBehaviour(None)

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
