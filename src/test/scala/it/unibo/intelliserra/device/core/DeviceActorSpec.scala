package it.unibo.intelliserra.device.core

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo}
import it.unibo.intelliserra.device.core.sensor.SensorActor
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DeviceActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var device: TestActorRef[DeviceActor] = _
  private var zoneManagerProbe: TestProbe = _
  private var zoneProbe: TestProbe = _
  private val zoneID = "ZONE1"
  private val deviceMocked = mockSensor("mockSensor")

  before {
    zoneManagerProbe = TestProbe()
    zoneProbe = TestProbe()
    device = TestActorRef.create(system, SensorActor.props(deviceMocked))
  }

  after {
    killActors(device)
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A device " must {
    "send an ack to confirm association" in {
      device.tell(AssociateTo(zoneProbe.ref, zoneID), zoneManagerProbe.ref)
      zoneManagerProbe.expectMsg(Ack)
    }
  }
}
