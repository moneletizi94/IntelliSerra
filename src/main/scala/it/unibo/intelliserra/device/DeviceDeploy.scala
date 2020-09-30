package it.unibo.intelliserra.device

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Messages.{JoinDevice, JoinError, JoinOK}
import it.unibo.intelliserra.core.entity.Device
import it.unibo.intelliserra.device.core.actuator.{Actuator, ActuatorActor}
import it.unibo.intelliserra.device.core.sensor.{Sensor, SensorActor}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * This trait allows you to insert new devices into the system.
 * Devices can be sensors or actuators.
 */
trait DeviceDeploy {

  /**
   * This method is used to ask at the [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]] to insert a new sensor in the system
   *
   * @param sensor the sensor to be inserted into the system
   * @return a Future[Unit] that is completed or failed following a message
   */
  def join(sensor: Sensor): Future[String]

  /**
   * This method is used to ask at the [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]] to insert a new actuator in the system
   *
   * @param actuator the actuator to be inserted into the system
   * @return a Future[Unit] that is completed or failed following a message
   */
  def join(actuator: Actuator): Future[String]
}

/**
 * Factory for DeviceDeploy instances.
 */
object DeviceDeploy {

  def apply(greenHouseName: String, serverAddress: String, serverPort: Int): DeviceDeploy =
    new DeviceDeployImpl(greenHouseName, serverAddress, serverPort)

  private[device] class DeviceDeployImpl(private val greenHouseName: String,
                                         private val serverAddress: String,
                                         private val serverPort: Int) extends DeviceDeploy {

    private implicit val actorSystem: ActorSystem = ActorSystem("device", GreenHouseConfig.client())
    private implicit val ec: ExecutionContext = actorSystem.dispatcher
    private implicit val timeout: Timeout = Timeout(5 seconds)

    private val entityManagerActor = actorSystem actorSelection RemotePath.entityManager(greenHouseName, serverAddress, serverPort)

    override def join(sensor: Sensor): Future[String] = {
      val sensorActor = SensorActor(sensor)(actorSystem)
      joinDevice(sensor, sensorActor)
    }

    override def join(actuator: Actuator): Future[String] = {
      val actuatorActor = ActuatorActor(actuator)(actorSystem)
      joinDevice(actuator, actuatorActor)
    }

    private def joinDevice(device: Device, deviceActor: ActorRef): Future[String] = {
      entityManagerActor ? JoinDevice(device.identifier, device.capability, deviceActor) flatMap {
        case JoinOK => Future.successful(device.identifier)
        case JoinError(error) => terminate(error, deviceActor)
      }
    }

    /**
     * Method to stop entityActor and complete future in case of failure.
     *
     * @return a Future[Unit]
     */
    private def terminate[T](error: String, entity: ActorRef): Future[T] = {
      actorSystem.stop(entity)
      Future.failed(new IllegalArgumentException(error))
    }

  }

}
