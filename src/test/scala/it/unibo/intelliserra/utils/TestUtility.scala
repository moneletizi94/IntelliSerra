package it.unibo.intelliserra.utils

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.entity.{Capability, EntityChannel, RegisteredSensor}
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, Sensor, StringType}
import monix.reactive.Observable

import scala.concurrent.{Await, Awaitable, Future}

trait TestUtility {

  import akka.util.Timeout

  import scala.concurrent.duration.{Duration, _}

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "mySerra"

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

  def mockSensor(sensorID: String): Sensor = new Sensor {
    override def readPeriod: FiniteDuration = 5 seconds
    override def read(): Measure = Measure(Temperature)(10)
    override def onInit(): Unit = {}
    override def onAssociateZone(zoneName: String): Unit = {}
    override def onDissociateZone(zoneName: String): Unit = {}
    override def capability: Capability.SensingCapability = SensingCapability(Temperature)
    override def identifier: String = sensorID
  }

  def mockActuator(actuatorID: String): Actuator = {
    new Actuator {
      override def capability: Capability.ActingCapability = Capability.acting(Water)
      override def actionHandler: ActionHandler = { case _ => Future.unit }
      override def identifier: String = actuatorID
      override def onInit(): Unit = {}
      override def onAssociateZone(zoneName: String): Unit = {}
      override def onDissociateZone(zoneName: String): Unit = {}
    }
  }

  case object Temperature extends Category[IntType]
  case object Humidity extends Category[IntType]
  case object Weather extends Category[StringType]

  case object Water extends Action

  def sensorEntityChannelFromRef(entityRef: ActorRef): EntityChannel = {
    EntityChannel(RegisteredSensor("sensor", SensingCapability(Temperature)), entityRef)
  }
}