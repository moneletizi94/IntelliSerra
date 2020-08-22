package it.unibo.intelliserra.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.Category
import it.unibo.intelliserra.common.communication._
import it.unibo.intelliserra.server.core.{RegisteredActuator, RegisteredSensor}
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
      val sensorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinSensor(mockSensorID, mockSensorCapability, sensorActorProbe.ref))
    }
  }

  "An Entity Manager" must {
    "register an actuator after receiving join" in {
      val actuatorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinActuator(mockActuatorID, mockActuatorCapability, actuatorActorProbe.ref))
    }
  }

  "An Entity Manager " should  {
    "not permit adding of sensor with existing identifier" in {
      val sensorActorProbe = TestProbe()
      checkNoDuplicateInsertion(JoinSensor(mockSensorID, mockSensorCapability, sensorActorProbe.ref))
    }
  }

  "An Entity Manager " should  {
    "not permit adding of actuator with existing identifier" in {
      val actuatorActorProbe = TestProbe()
      checkNoDuplicateInsertion(JoinSensor(mockSensorID, mockSensorCapability, actuatorActorProbe.ref))
    }
  }

  "An Entity Manager just created " should  {
    "have no entities" in {
      entityManager.underlyingActor.entities shouldBe Map()
    }
  }

  private def sendJoinEntityMessage(joinRequestMessage: JoinRequest){
    entityManager ! joinRequestMessage
    expectMsg(JoinOK)
    joinRequestMessage match {
      case JoinSensor(identifier, sensingCapability, sensorRef) => {
        entityManager.underlyingActor.entities shouldBe Map(RegisteredSensor(identifier, sensingCapability) -> sensorRef)
      }
      case JoinActuator(identifier, actingCapability, actuatorRef) => {
        entityManager.underlyingActor.entities shouldBe Map(RegisteredActuator(identifier, actingCapability) -> actuatorRef)
      }
    }
  }

  private def checkNoDuplicateInsertion(joinRequest: JoinRequest) = {
    entityManager ! joinRequest
    entityManager.underlyingActor.entities should have size 1
    entityManager ! joinRequest
    entityManager.underlyingActor.entities should have size 1
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
