package it.unibo.intelliserra.device

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Messages.{ JoinError, JoinOK, JoinDevice}
import it.unibo.intelliserra.device.core.{Actuator, Sensor}
import it.unibo.intelliserra.device.core.actuator.ActuatorActor
import it.unibo.intelliserra.device.core.sensor.SensorActor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 */
trait DeviceDeploy {
  def join(sensor: Sensor): Future[String]
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
    private implicit val timeout : Timeout = Timeout(5 seconds)

    private val entityManagerActor = actorSystem actorSelection RemotePath.entityManager(greenHouseName, serverAddress, serverPort)

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]] to insert a new sensor in the system
     *
     * @param sensor the sensor to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def join(sensor: Sensor): Future[String] = {
      val sensorActor = SensorActor(sensor)(actorSystem)
      entityManagerActor ? JoinDevice(sensor.identifier, sensor.capability, sensorActor) flatMap {
        case JoinOK => Future.successful(sensor.identifier)
        case JoinError(error) => terminate(error, sensorActor)
      }
    }

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]] to insert a new actuator in the system
     *
     * @param actuator the actuator to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def join(actuator: Actuator): Future[String] = {
      val actuatorActor = ActuatorActor(actuator)(actorSystem)
      entityManagerActor ? JoinDevice(actuator.identifier, actuator.capability, actuatorActor) flatMap {
        case JoinOK => Future.successful(actuator.identifier)
        case JoinError(error) => terminate(error, actuatorActor)
      }
    }

    /**
     * Method to stop entityActor and complete future in case of failure.
     *
     * @return a Future[Unit]
     */
    private def terminate[T](error : String, entity: ActorRef): Future[T] = {
      actorSystem.stop(entity)
      Future.failed(new IllegalArgumentException(error))
    }

  }
}
