package it.unibo.intelliserra.server

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit}
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
private class GreenHouseControllerSpec extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with BeforeAndAfter
  with TestUtility {

  private val mockSensorID = "sensorID"
  private val mockZoneID = "zone1"
  private var greenHouseController : TestActorRef[GreenHouseController] = _

  before {
    greenHouseController = TestActorRef.create[GreenHouseController](system, Props[GreenHouseController])
  }
/*
  "A greenHouseController " must {
    "ask for create zone" in {
      greenHouseController ? CreateZone(mockZoneID)
      expectMsg(ZoneCreated)
    }
  }

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
