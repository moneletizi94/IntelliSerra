package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.server.EntityManager.{JoinActuator, JoinError, JoinOK, JoinSensor, RegisteredActuator, RegisteredSensor}

import scala.util.Try

class EntityManagerActor extends Actor{

  private[server] var sensors : Map[RegisteredSensor, ActorRef] = Map()
  private[server] var actuators : Map[RegisteredActuator, ActorRef] = Map()

  override def receive: Receive = {
    case JoinSensor(identifier, capabilities, sensorRef) => {
      val joinResult = Some(JoinOK).filterNot(_ => checkSensorExistence(identifier))
                                    .foreach(_ => sensors = sensors + (RegisteredSensor(identifier,capabilities) -> sensorRef))

      sender ! joinResult
    }
  }

  private def checkSensorExistence(identifier:String) = sensors.exists(_._1.identifier == identifier)
  //private def checkActuatorExistence(identifier: String) = actuators.exists(_._1.identifier == identifier)
}

object EntityManager{
  val name = "entityManager"
  def apply(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props[EntityManagerActor], name)
  }
  case class RegisteredActuator(identifier: String, capability : ActingCapability)
  case class RegisteredSensor(identifier: String, capability: SensingCapability)
  case class JoinSensor(identifier: String, sensingCapability: SensingCapability, sensorRef: ActorRef)
  case class JoinActuator(identifier: String, actingCapability: ActingCapability, actuatorRef : ActorRef)

  trait JoinResult
  case object JoinOK extends JoinResult
  case object JoinError extends JoinResult
}
