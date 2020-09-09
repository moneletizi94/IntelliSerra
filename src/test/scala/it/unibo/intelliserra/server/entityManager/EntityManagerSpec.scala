package it.unibo.intelliserra.server.entityManager

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.entity._
import it.unibo.intelliserra.server.entityManager.EMEventBus.PublishedOnRemoveEntity
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Categories.Temperature
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
private class EntityManagerSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var entityManager : TestActorRef[EntityManagerActor] = _
  private var mockZoneManager: TestProbe = _
  private val mockSensorID = "sensorID"
  private val mockSensorCapability = Capability.sensing(Temperature)
  private val mockActuatorID = "actuatorID"
  private val mockActuatorCapability = Capability.acting(Set[Action]())

  before{
    mockZoneManager = TestProbe()
    entityManager = TestActorRef.create(system, Props[EntityManagerActor])
  }

  "An Entity Manager" should {

    "register a sensor after receiving join" in {
      val sensorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinSensor(mockSensorID, mockSensorCapability, sensorActorProbe.ref))
    }

    "register an actuator after receiving join" in {
      val actuatorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinActuator(mockActuatorID, mockActuatorCapability, actuatorActorProbe.ref))
    }

    "not permit adding of sensor with existing identifier" in {
      val sensorActorProbe = TestProbe()
      checkNoDuplicateInsertion(JoinSensor(mockSensorID, mockSensorCapability, sensorActorProbe.ref))
    }

    "not permit adding of actuator with existing identifier" in {
      val actuatorActorProbe = TestProbe()
      checkNoDuplicateInsertion(JoinSensor(mockSensorID, mockSensorCapability, actuatorActorProbe.ref))
    }

    "have no entities" in {
      entitiesInEMShouldBe(List())
    }

    /* --- START TESTING REMOVE ENTITY --- */
    "not allow to delete a nonexistent entity" in {
      entityManager ! RemoveEntity(mockSensorID)
      expectMsg(EntityNotFound)
    }

    "delete an existing entity" in {
      val actuatorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinActuator(mockActuatorID, mockActuatorCapability, actuatorActorProbe.ref))
      EMEventBus.subscribe(mockZoneManager.ref, EMEventBus.topic)
      entityManager ! RemoveEntity(mockActuatorID)
      expectMsg(EntityRemoved)
      mockZoneManager.expectMsgType[PublishedOnRemoveEntity]
      entitiesInEMShouldBe(List())
    }
    /* --- END TESTING REMOVE ENTITY --- */

  }

  private def sendJoinEntityMessage(joinRequestMessage: JoinRequest){
    entityManager ! joinRequestMessage
    expectMsg(JoinOK)
    joinRequestMessage match {
      case JoinSensor(identifier, sensingCapability, sensorRef) =>
        entitiesInEMShouldBe(List(EntityChannel(RegisteredSensor(identifier, sensingCapability), sensorRef)))
      case JoinActuator(identifier, actingCapability, actuatorRef) =>
        entitiesInEMShouldBe(List(EntityChannel(RegisteredActuator(identifier, actingCapability), actuatorRef)))
    }
  }

  private def entitiesInEMShouldBe(result: List[EntityChannel]) = {
    entityManager.underlyingActor.entities shouldBe result
  }
  private def checkNoDuplicateInsertion(joinRequest: JoinRequest) = {
    entityManager ! joinRequest
    expectMsg(JoinOK)
    entityManager.underlyingActor.entities should have size 1
    entityManager ! joinRequest
    expectMsgType[JoinError]
    entityManager.underlyingActor.entities should have size 1
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
