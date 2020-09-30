package it.unibo.intelliserra.server.entityManager

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.core.action.Action
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
  private val mockActuatorCapability = Capability.acting()

  before{
    mockZoneManager = TestProbe()
    entityManager = TestActorRef.create(system, Props[EntityManagerActor])
  }

  "An Entity Manager" should {

    "register a device after receiving join" in {
      val sensorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinDevice(mockSensorID, mockSensorCapability, sensorActorProbe.ref))
    }

    "not allow the addition of an actuator with an existing identifier" in {
      val actuatorActorProbe = TestProbe()
      checkNoDuplicateInsertion(JoinDevice(mockSensorID, mockSensorCapability, actuatorActorProbe.ref))
    }

    "have no entities" in {
      entityManager.underlyingActor.entities shouldBe List()
    }

    /* --- START TESTING REMOVE ENTITY --- */
    "not allow to delete a nonexistent entity" in {
      entityManager ! RemoveEntity(mockSensorID)
      expectMsg(EntityNotFound)
    }

    "delete an existing entity" in {
      val actuatorActorProbe = TestProbe()
      sendJoinEntityMessage(JoinDevice(mockActuatorID, mockActuatorCapability, actuatorActorProbe.ref))
      EMEventBus.subscribe(mockZoneManager.ref, EMEventBus.topic)
      entityManager ! RemoveEntity(mockActuatorID)
      expectMsg(EntityRemoved)
      mockZoneManager.expectMsgType[PublishedOnRemoveEntity]
      entityManager.underlyingActor.entities shouldBe List()
    }
    /* --- END TESTING REMOVE ENTITY --- */

  }

  private def sendJoinEntityMessage(joinRequestMessage: JoinDevice): Unit = {
    entityManager ! joinRequestMessage
    expectMsg(JoinOK)
    val registeredDevice = RegisteredDevice(joinRequestMessage.identifier, joinRequestMessage.capability)
    entityManager.underlyingActor.entities shouldBe List(DeviceChannel(registeredDevice, joinRequestMessage.deviceRef))
  }

  private def checkNoDuplicateInsertion(joinRequest: JoinDevice) = {
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
