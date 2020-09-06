package it.unibo.intelliserra.utils

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.{EntityChannel, RegisteredSensor}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, Sensor, StringType}
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.utils.TestUtility.Actions._
import it.unibo.intelliserra.utils.TestUtility.Categories._
import it.unibo.intelliserra.utils.TestUtility.Actions.Water
import it.unibo.intelliserra.utils.TestUtility.Categories.Temperature
import monix.reactive.Observable

import scala.concurrent.{Await, Awaitable, Future}
import scala.util.Random

trait TestUtility {

  import akka.util.Timeout

  import scala.concurrent.duration.{Duration, _}

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "mySerra"
  val defaultServerConfig: ServerConfig = ServerConfig(GreenhouseName, Hostname, Port)

  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val duration: FiniteDuration = 5 seconds

  def awaitResult[T](awaitable: Awaitable[T])(implicit duration: Duration): T = Await.result(awaitable, duration)
  def awaitReady[T](awaitable: Awaitable[T])(implicit duration: Duration): awaitable.type = Await.ready(awaitable, duration)

  def killActors(actors: ActorRef*)(implicit actorSystem: ActorSystem): Unit = {
    val testProbe = TestProbe()
    actors.foreach { testProbe.watch }
    actors.foreach {
      actor =>
        actorSystem.stop(actor)
        testProbe.expectTerminated(actor, duration)
    }
  }

  /**
   * This is an utility method used in tests. It mocks a Sensor given the sensorID
   * @param sensorID the identifier of the sensor
   * @return Sensor
   */
  def mockSensor(sensorID: String): Sensor = {
    new Sensor {
      override def identifier: String = sensorID

      override def capability: SensingCapability = SensingCapability(Temperature)

      override def measures: Observable[Measure] = Observable()
    }
  }

  /**
   * This is an utility method used in tests. It mocks an Actuator given the actuatorID
   * @param actuatorID the identifier of the actuator
   * @return Actuator
   */
  def mockActuator(actuatorID: String): Actuator = {
    new Actuator {
      override def identifier: String = actuatorID

      override def capability: ActingCapability = ActingCapability(Set(Water))

      override def state: Observable[OperationalState] = Observable()

      override def actionHandler: ActionHandler = {
        case _ => Future.successful(Idle)
      }
    }
  }

  def sendNMessageFromNProbe[T](messagesNumber: Int, sendTo : ActorRef, message : T)(implicit system: ActorSystem): Unit = {
    for {
      _ <- 1 to messagesNumber
      sensor = TestProbe()
    } sendTo.tell(message, sensor.ref)
  }

  /**
   * This is an utility method used to create an EntityChannel given an actorRef
   * @param entityRef actorRef of the entityChannel
   * @return
   */
  def sensorEntityChannelFromRef(entityRef: ActorRef): EntityChannel = {
    EntityChannel(RegisteredSensor("sensor", SensingCapability(Temperature)), entityRef)
  }

}

object TestUtility{
  object Categories{
    case object Temperature extends Category[IntType]
    case object Humidity extends Category[IntType]
    case object Weather extends Category[StringType]
  }

  object Actions{
    case object Water extends Action
    case object Light extends Action
    case object Fan extends Action
  }
  case object Water extends Action

}

