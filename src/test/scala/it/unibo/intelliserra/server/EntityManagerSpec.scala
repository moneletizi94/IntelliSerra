package it.unibo.intelliserra.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages.{JoinActuator, JoinOK, JoinRequest, JoinSensor}
import it.unibo.intelliserra.core.entity._
import it.unibo.intelliserra.core.sensor.Category
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import it.unibo.intelliserra.utils.TestUtility

@RunWith(classOf[JUnitRunner])
private class EntityManagerSpec extends TestKit(ActorSystem("MySpec")) with TestUtility
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
      entityManager.underlyingActor.entities shouldBe List()
    }
  }

  private def sendJoinEntityMessage(joinRequestMessage: JoinRequest){
    entityManager ! joinRequestMessage
    expectMsg(JoinOK)
    joinRequestMessage match {
      case JoinSensor(identifier, sensingCapability, sensorRef) =>
        entityManager.underlyingActor.entities shouldBe List(EntityChannel(RegisteredSensor(identifier, sensingCapability), sensorRef))
      case JoinActuator(identifier, actingCapability, actuatorRef) =>
        entityManager.underlyingActor.entities shouldBe List(EntityChannel(RegisteredActuator(identifier, actingCapability), actuatorRef))
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
