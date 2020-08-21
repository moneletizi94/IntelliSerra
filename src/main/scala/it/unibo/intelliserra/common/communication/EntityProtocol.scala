package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}

trait EntityProtocol {

  case class JoinSensor(identifier: String, sensingCapability: SensingCapability, sensorRef: ActorRef)
  case class JoinActuator(identifier: String, actingCapability: ActingCapability, actuatorRef : ActorRef)

  sealed trait JoinResult
  case object JoinOK extends JoinResult
  case class JoinError(error:String) extends JoinResult
}
