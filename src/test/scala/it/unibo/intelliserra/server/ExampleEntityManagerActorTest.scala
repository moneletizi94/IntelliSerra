package it.unibo.intelliserra.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.Category
import it.unibo.intelliserra.server.EntityManager.{JoinSensor, RegisteredActuator, RegisteredSensor}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
private class ExampleEntityManagerTest extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll  {

  private val entityManager : TestActorRef[EntityManagerActor] = TestActorRef.create[EntityManagerActor](system, Props[EntityManagerActor])
  private val mockSensorID = "sensorID"
  private val mockSensorCapability = SensingCapability(Temperature)
  private val mockActuatorID = "actuatorID"
  private val mockActuatorCapability = ActingCapability(Set())

  case object Temperature extends Category
  case object Humidity extends Category

  "An Entity Manager" must {
    "register a sensor after receiving join" in {
      val sensorProbe = TestProbe()

      entityManager ! JoinSensor(mockSensorID, mockSensorCapability, sensorProbe.ref)
      entityManager.underlyingActor.sensors shouldBe Map(RegisteredSensor(mockSensorID, mockSensorCapability) -> sensorProbe.ref)
    }
  }


}
