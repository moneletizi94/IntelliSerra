package it.unibo.intelliserra.device

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Messages.{JoinActuator, JoinError, JoinOK, JoinSensor}
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.core.actuator.ActuatorActor
import it.unibo.intelliserra.device.core.sensor.SensorActor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 */
trait DeviceDeploy {
  def deploySensor(sensor: Sensor): Future[Unit]
  def deployActuator(actuator: Actuator): Future[Unit]
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
     * This method is used to ask at the [[it.unibo.intelliserra.server.EntityManagerActor]] to insert a new sensor in the system
     *
     * @param sensor the sensor to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def deploySensor(sensor: Sensor): Future[Unit] = {
      val sensorActor = SensorActor(sensor)(actorSystem)
      entityManagerActor ? JoinSensor(sensor.identifier, sensor.capability, sensorActor) flatMap {
        case JoinOK => Future.successful(():Unit)
        case JoinError(error) => terminate(error, sensorActor)
      }
    }

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.EntityManagerActor]] to insert a new actuator in the system
     *
     * @param actuator the actuator to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def deployActuator(actuator: Actuator): Future[Unit] = {
      val actuatorActor = ActuatorActor(actuator)(actorSystem)
      entityManagerActor ? JoinActuator(actuator.identifier, actuator.capability, actuatorActor) flatMap {
        case JoinOK => Future.successful(():Unit)
        case JoinError(error) => terminate(error, actuatorActor)
      }
    }

    /**
     * Method to stop entityActor and complete future in case of failure.
     *
     * @return a Future[Unit]
     */
    private def terminate(error : String, entity: ActorRef): Future[Unit] = {
      actorSystem.stop(entity)
      Future.failed(new IllegalArgumentException(error))
    }

  }
}
