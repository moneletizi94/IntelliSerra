package it.unibo.intelliserra.device

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.server.{ActuatorActor, EntityManager, SensorActor}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import it.unibo.intelliserra.common.communication.Protocol._

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

  def apply(serverUri: String): DeviceDeploy = new DeviceDeployImpl(serverUri)

  private[device] class DeviceDeployImpl(val entityManagerPath: String) extends DeviceDeploy {

    private implicit val actorSystem: ActorSystem = ActorSystem("device", GreenHouseConfig.client())
    private implicit val ec: ExecutionContext = actorSystem.dispatcher
    private implicit val timeout : Timeout = Timeout(5 seconds)

    private val entityManagerActor = actorSystem actorSelection entityManagerPath

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.EntityManager]] to insert a new sensor in the system
     *
     * @param sensor the sensor to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def deploySensor(sensor: Sensor): Future[Unit] = {
      val sensorActor = SensorActor(sensor)(actorSystem)
      entityManagerActor ? JoinSensor(sensor.identifier, sensor.capability, sensorActor) flatMap {
        case JoinOK => Future.unit
        case JoinError(error) => terminate(error, sensorActor)
      }
    }

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.EntityManager]] to insert a new actuator in the system
     *
     * @param actuator the actuator to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def deployActuator(actuator: Actuator): Future[Unit] = {
      val actuatorActor = ActuatorActor(actuator)(actorSystem)
      entityManagerActor ? JoinActuator(actuator.identifier, actuator.capability, actuatorActor) flatMap {
        case JoinOK => Future.unit
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
