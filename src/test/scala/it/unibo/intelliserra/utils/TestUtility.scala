package it.unibo.intelliserra.utils

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor}
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

  def mockSensor(sensorID: String): Sensor = {
    new Sensor {
      override def identifier: String = sensorID

      override def capability: SensingCapability = SensingCapability(Temperature)

      override def measures: Observable[Measure] = Observable()
    }
  }
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
  case object Temperature extends Category
  case object Water extends Action
}