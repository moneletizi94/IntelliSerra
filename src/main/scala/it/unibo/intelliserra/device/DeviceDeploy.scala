package it.unibo.intelliserra.device

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.server.{ActuatorActor, EntityManager, SensorActor}
import it.unibo.intelliserra.server.EntityManager.{JoinActuator, JoinSensor}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 */
trait DeviceDeploy{
  def deploySensor(sensor: Sensor): Future[Unit]
  def deployActuator(actuator: Actuator): Future[Unit]
}

/**
 * Factory for DeviceDeploy instances.
 */
 object DeviceDeploy {

  trait JoinResult
   case object JoinOK extends JoinResult
   case object JoinError extends JoinResult

  def apply()(implicit actorSystem: ActorSystem): DeviceDeploy = new DeviceDeployImpl()

  private[device] class DeviceDeployImpl(private implicit val actorSystem: ActorSystem) extends DeviceDeploy {

    private implicit val ec: ExecutionContext = actorSystem.dispatcher
    private implicit val timeout : Timeout = Timeout(5 seconds)
    private val entityActor = EntityManager()

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.EntityManager]] to insert a new sensor in the system
     *
     * @param sensor the sensor to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def deploySensor(sensor: Sensor): Future[Unit] = {
      val sensorActor = SensorActor(sensor)
      entityActor ? JoinSensor(sensor.identifier, sensor.capability, sensorActor) flatMap{
        case JoinOK => Future.unit
        case JoinError => terminate
        //case a: Any => println(a); Future.unit
      }
    }

    /**
     * This method is used to ask at the [[it.unibo.intelliserra.server.EntityManager]] to insert a new actuator in the system
     *
     * @param actuator the actuator to be inserted into the system
     * @return a Future[Unit] that is completed or failed following a message
     */
    override def deployActuator(actuator: Actuator): Future[Unit] = {
      val actuatorActor = ActuatorActor(actuator)
      entityActor ? JoinActuator(actuator.identifier, actuator.capability, actuatorActor) flatMap{
        case JoinOK => Future.unit
        case JoinError => terminate()
      }
    }

    /**
     * Method to stop entityActor and complete future in case of failure.
     *
     * @return a Future[Unit]
     */
    def terminate(): Future[Unit] = {
      actorSystem.stop(entityActor)
      actorSystem.terminate().flatMap(_ => Future.failed(new IllegalArgumentException("identifier already exists")))
    }

  }
}
