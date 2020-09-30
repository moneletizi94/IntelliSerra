package it.unibo.intelliserra.server.entityManager

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import it.unibo.intelliserra.server.entityManager.EMEventBus.PublishedOnRemoveEntity
import it.unibo.intelliserra.server.zone.ZoneManagerActor
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EMEventBusSpec extends TestKit(ActorSystem("MySpec"))
with ImplicitSender
with Matchers
with WordSpecLike
with BeforeAndAfter
with BeforeAndAfterAll
with TestUtility {

  private val subscriber = TestProbe().ref
  private val correctTopic = EMEventBus.topic
  private val wrongTopic = "WrongTopic"

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An EMEventBus" should {

    "refuse a subscriber for the wrong topic" in {
      assertThrows[IllegalArgumentException](EMEventBus.subscribe(subscriber, wrongTopic))
    }

    "accept a subscriber for the correct topic" in {
      EMEventBus.subscribe(subscriber, correctTopic) shouldBe true
    }

    "refuse to publish on the wrong topic" in {
      val sensor = TestProbe().ref
      assertThrows[IllegalArgumentException](EMEventBus.publish(wrongTopic, PublishedOnRemoveEntity(sensorEntityChannelFromRef(sensor))))
    }

    "accept to publish on the correct topic" in {
      val sensor = TestProbe().ref
      EMEventBus.subscribe(subscriber, correctTopic)
      EMEventBus.publish(correctTopic, PublishedOnRemoveEntity(sensorEntityChannelFromRef(sensor)))
    }
  }
}
