package it.unibo.intelliserra.device

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.server.{ActuatorActor, EntityManager, SensorActor}
import it.unibo.intelliserra.server.EntityManager.{JoinActuator, JoinResult, JoinSensor}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait DeviceDeploy{
  def deploySensor(sensor: Sensor): Future[Unit]
  def deployActuator(actuator: Actuator): Future[Unit]
}

 object DeviceDeploy extends DeviceDeploy {
   case object JoinOK extends JoinResult
   case object JoinError extends JoinResult

   private implicit val actorSystem: ActorSystem = ActorSystem()
   private implicit val ec: ExecutionContext = actorSystem.dispatcher
   private implicit val timeout : Timeout = Timeout(5 seconds)
   private val entityActor = EntityManager.apply

   override def deploySensor(sensor: Sensor): Future[Unit] = {
     val sensorActor = SensorActor.apply(sensor)
     entityActor ? JoinSensor(sensor.identifier, sensor.capability, sensorActor) flatMap{
       case JoinOK => Future.unit
       case JoinError => terminate()
     }
   }

   override def deployActuator(actuator: Actuator): Future[Unit] = {
     val actuatorActor = ActuatorActor.apply(actuator)
     entityActor ? JoinActuator(actuator.identifier, actuator.capability, actuatorActor) flatMap{
       case JoinOK => Future.unit
       case JoinError => terminate()
     }
   }

   def terminate(): Future[Unit] = {
     actorSystem.stop(entityActor)
     actorSystem.terminate().flatMap(_ => Future.failed(new IllegalArgumentException("identifier already exists")))
   }
}
