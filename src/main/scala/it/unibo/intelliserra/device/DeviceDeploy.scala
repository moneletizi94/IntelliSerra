package it.unibo.intelliserra.device

import akka.actor.ActorSystem
import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor

import scala.concurrent.Future

trait DeviceDeploy {
  def deploySensor(sensor: Sensor): Future[Unit]
  def deployActuator(actuator: Actuator): Future[Unit]
}

 object DeviceDeploy{
   //private val entityManager: EntityManager
   private implicit val actorSystem: ActorSystem = ActorSystem()

   //def terminate(): Future[Unit] = {
     //actorSystem.stop(entityActor)
     //actorSystem.terminate().flatMap(_ => Future.unit)
   //}
}
