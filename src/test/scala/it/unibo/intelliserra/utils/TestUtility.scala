package it.unibo.intelliserra.utils

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.perception.{BooleanType, Category, CharType, DoubleType, IntType, StringType}
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.rule.{Rule, StatementTestUtils}
import it.unibo.intelliserra.device.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.device.core.actuator.{Actuator, Operation}
import it.unibo.intelliserra.device.core.sensor.Sensor
import it.unibo.intelliserra.examples.RuleDslExample.{Temperature, Water}
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.entityManager.{DeviceChannel, RegisteredDevice}

import scala.concurrent.{Await, Awaitable}

trait TestUtility extends StatementTestUtils {

  import akka.util.Timeout

  import scala.concurrent.duration.{Duration, _}

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "mySerra"
  val actionSet: Set[Action] = Set(Water)
  val rule: Rule = Temperature >20 executeMany actionSet
  val defaultServerConfig: ServerConfig = ServerConfig(GreenhouseName, Hostname, Port)
  val defaultConfigWithRule: ServerConfig = ServerConfig(GreenhouseName, Hostname, Port, rules = List(rule))

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
  def mockTemperatureSensor(sensorID: String): Sensor =
    Sensor(sensorID, Temperature, 5 seconds)(Stream.continually(10))

  /**
   * This is an utility method used in tests. It mocks an Actuator given the actuatorID
   * @param actuatorID the identifier of the actuator
   * @return Actuator
   */
  def mockActuator(actuatorID: String): Actuator = mockActuator(actuatorID, Capability.acting(Water.getClass)){ case _ => Operation.completed() }
  def mockActuator(actuatorID: String, actingCapability: ActingCapability)(handler: ActionHandler): Actuator =
    Actuator(actuatorID, actingCapability)(handler)

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
  def sensorEntityChannelFromRef(entityRef: ActorRef): DeviceChannel = {
    DeviceChannel(RegisteredDevice("sensor", SensingCapability(Temperature)), entityRef)
  }

  implicit def fromProbeToRef(testProbe: TestProbe) : ActorRef = testProbe.ref
}

object TestUtility{
  object Categories{
    case object Temperature extends Category[IntType]
    case object Humidity extends Category[IntType]
    case object Weather extends Category[StringType]
    case object LightToggle extends Category[BooleanType]
    case object Pressure extends Category[DoubleType]
    case object CharCategory extends Category[CharType]
  }

  object Actions{
    case object Water extends Action
    case object Light extends Action
    case object Fan extends Action
    case object OpenWindow extends Action
  }
}