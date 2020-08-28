package it.unibo.intelliserra.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.zone.ZoneManagerActor
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
private class GreenHouseControllerSpec extends TestKit(ActorSystem("GreenHouseControllerMySpec"))
  with WordSpecLike
  with BeforeAndAfter
  with TestUtility
  with ImplicitSender
  with BeforeAndAfterAll{

  private var mockEntityID = "sensorID"
  private var mockZoneID: String = _

  private var greenHouseController : TestActorRef[GreenHouseController] = _
  private var entityManagerActor : ActorRef = _
  private var zoneManagerActor : ActorRef = _

  before{
    entityManagerActor = EntityManagerActor()
    zoneManagerActor = ZoneManagerActor()
    greenHouseController = TestActorRef.create(system, Props(new GreenHouseController(zoneManagerActor, entityManagerActor)))
  }

  after{
    killActors(entityManagerActor, zoneManagerActor, greenHouseController)
  }

  "A greenHouseController " must {
    "ask for create zone" in {
      mockZoneID = "zone"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
    }
  }

  "A greenHouseController " must {
    "ask for create zone that already exists" in {
      mockZoneID ="zone1"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Conflict))
    }
  }

  "A greenHouseController " must {
    "ask for remove an existing zone" in {
      mockZoneID = "zone2"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(Deleted))
    }
  }

  "A greenHouseController " must {
    "ask for remove zone that already exists" in {
      mockZoneID ="zone3"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(Deleted))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(NotFound, "Zone not found"))
    }
  }

  "A greenHouseController " must {
    "ask to obtain the list of zones" in {
      mockZoneID ="zone4"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! GetZones()
      expectMsg(ServiceResponse(Ok, List(mockZoneID)))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(Deleted))
      greenHouseController ! GetZones()
      expectMsg(ServiceResponse(NotFound, "No zones!"))
    }
  }

  "A greenHouseController " must {
    "ask to assign entity to zone" in {
      mockZoneID ="zone5"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! AssignEntity(mockZoneID, mockEntityID)
      expectMsg(ServiceResponse(Ok))
    }
  }

}
