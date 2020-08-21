package it.unibo.intelliserra.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.Category
import it.unibo.intelliserra.server.EntityManager.{JoinOK, JoinSensor, RegisteredActuator, RegisteredSensor}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
private class ExampleEntityManagerSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll  {

  private var entityManager : TestActorRef[EntityManagerActor] = _
  private val mockSensorID = "sensorID"
  private val mockSensorCapability = SensingCapability(Temperature)
  private val mockActuatorID = "actuatorID"
  private val mockActuatorCapability = ActingCapability(Set())

  case object Temperature extends Category
  case object Humidity extends Category

  before{
    entityManager = TestActorRef.create[EntityManagerActor](system, Props[EntityManagerActor])
  }

  "An Entity Manager" must {
    "register a sensor after receiving join" in {
      val sensorProbe = TestProbe()
      entityManager ! JoinSensor(mockSensorID, mockSensorCapability, sensorProbe.ref)
      expectMsg(JoinOK)
      entityManager.underlyingActor.entities shouldBe Map(RegisteredSensor(mockSensorID, mockSensorCapability) -> sensorProbe.ref)
    }
  }

  "An Entity Manager " should  {
    "not permit adding of entity with existing identifier" in {
      val sensorProbe = TestProbe()
      entityManager ! JoinSensor(mockSensorID, mockSensorCapability, sensorProbe.ref)
      entityManager.underlyingActor.entities should have size 1
      entityManager ! JoinSensor(mockSensorID, mockSensorCapability, sensorProbe.ref)
      entityManager.underlyingActor.entities should have size 1
    }
  }

  "An Entity Manager just created " should  {
    "have no entities" in {
      entityManager.underlyingActor.entities shouldBe Map()
    }
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
