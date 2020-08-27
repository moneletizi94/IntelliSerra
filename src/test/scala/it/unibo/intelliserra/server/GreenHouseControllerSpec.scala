package it.unibo.intelliserra.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.actor.DefaultTimeout
import it.unibo.intelliserra.common.communication.Messages
import it.unibo.intelliserra.common.communication.Protocol.{Conflict, CreateZone, Created}
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
private class GreenHouseControllerSpec extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with BeforeAndAfter
  with TestUtility {

  private val mockSensorID = "sensorID"
  private val mockZoneID = "zone1"
  override implicit val timeout : Timeout =  Timeout(5 seconds)

  private var greenHouseController : TestActorRef[GreenHouseController] = TestActorRef.create(system, Props[GreenHouseController]())

  before {
    greenHouseController = TestActorRef.create(system, Props[GreenHouseController]())

  }

  "A greenHouseController " must {
    "ask for create zone" in {
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(Created)
    }
  }/*

  "A greenHouseController " must {
    "ask for create zone that already exists" in {
      greenHouseController ? CreateZone(mockZoneID)
      expectMsg(ZoneCreated)
      greenHouseController ? CreateZone(mockZoneID)
      expectMsg(NoZone)
    }
  }

  "A greenHouseController " must {
    "ask for remove existing zone" in {
      greenHouseController ? CreateZone(mockZoneID)
      expectMsg(ZoneCreated)
      greenHouseController ? RemoveZone(mockZoneID)
      expectMsg(ZoneRemoved)
    }
  }
*/
}
