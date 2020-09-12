package it.unibo.intelliserra.utils

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.actuator.{Action, Actuator, TimedTask}
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.entity.{Capability, EntityChannel, RegisteredSensor}
import it.unibo.intelliserra.core.rule.{Rule, StatementTestUtils}
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, Sensor, StringType}
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.utils.TestDevice.{TestActuator, TestSensor}
import it.unibo.intelliserra.utils.TestUtility.Actions.Water
import it.unibo.intelliserra.utils.TestUtility.Categories.Temperature
import it.unibo.intelliserra.core.rule.dsl._

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
  def mockSensor(sensorID: String): Sensor = mockSensor(sensorID, Capability.sensing(Temperature), 5 seconds, Stream.continually(Measure(Temperature)(10)))
  def mockSensor(sensorID: String, sensorCapability: SensingCapability, period: FiniteDuration, measures: Stream[Measure]): Sensor =
    TestSensor(sensorID, sensorCapability, period, measures)

  /**
   * This is an utility method used in tests. It mocks an Actuator given the actuatorID
   * @param actuatorID the identifier of the actuator
   * @return Actuator
   */
  def mockActuator(actuatorID: String): Actuator = mockActuator(actuatorID, Capability.acting(Water)){ case _ => TimedTask.now(Water) }
  def mockActuator(actuatorID: String, actingCapability: ActingCapability)(handler: ActionHandler): Actuator =
    TestActuator(actuatorID, actingCapability)(handler)

  /**
   * This is an utility method used to create an EntityChannel given an actorRef
   * @param entityRef actorRef of the entityChannel
   * @return
   */
  def sensorEntityChannelFromRef(entityRef: ActorRef): EntityChannel = {
    EntityChannel(RegisteredSensor("sensor", SensingCapability(Temperature)), entityRef)
  }
}

object TestUtility {
  object Categories{
    case object Temperature extends Category[IntType]
    case object Humidity extends Category[IntType]
    case object Weather extends Category[StringType]
  }

  object Actions{
    case object Water extends Action
    case object Light extends Action
    case object Fan extends Action
    case object OpenWindow extends Action
  }
}