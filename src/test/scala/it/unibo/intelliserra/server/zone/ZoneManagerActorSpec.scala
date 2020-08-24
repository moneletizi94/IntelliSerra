package it.unibo.intelliserra.server.zone

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Protocol._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ZoneManagerActorSpec extends TestKit(ActorSystem("MyTest"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private val zoneIdentifier = "Zone1"
  private val zoneIdentifier2 = "Zone2"
  private val zoneIdentifierNotAdded = "FakeZone"
  private var zoneManager: TestActorRef[ZoneManagerActor] = _

  before {
    zoneManager =  TestActorRef.create(system, Props[ZoneManagerActor]())
  }
  after {
    zoneManager.underlyingActor.zones.foreach({case (identifier, _) =>
      zoneManager ! RemoveZone(identifier)
      expectMsg(ZoneRemoved)
    })
  }

  "A zoneManagerActor" must {
    "create a zone with a never-used identifier" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      zoneManager ! ZoneExists(zoneIdentifier)
      expectMsgType[Zone]
    }
  }

  "A zoneManagerActor" must {
    "refuse the creation of a zone with a yet-used identifier" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreationError)
    }
  }


  "A zoneManagerActor" should {
    "not have a nonexistent identifier" in {
      zoneManager ! ZoneExists(zoneIdentifierNotAdded)
      expectMsg(NoZone)
    }
  }

  "A zoneManagerActor" must {
    "delete an existing zone when requested" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      zoneManager ! RemoveZone(zoneIdentifier)
      expectMsg(ZoneRemoved)
      zoneManager ! ZoneExists(zoneIdentifier)
      expectMsg(NoZone)
    }
  }

  "A zoneManagerActor" must {
    "refuse to delete a nonexistent zone when requested" in {
      zoneManager ! RemoveZone(zoneIdentifierNotAdded)
      expectMsg(NoZone)
    }

  "A zoneManagerActor" must {
    "return an empty list when it hasn't zones" in {
      zoneManager ! GetZones
      val zones = expectMsgPF() {
        case Zones(zones: List[String]) => zones
      }
      zones shouldBe List()
    }
  }

  "A zoneManagerActor" must {
    "return a list containing created zones" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      zoneManager ! CreateZone(zoneIdentifier2)
      expectMsg(ZoneCreated)
      zoneManager ! GetZones
      val zones = expectMsgPF() {
        case Zones(zones: List[String]) => zones
      }
      zones shouldBe List(zoneIdentifier, zoneIdentifier2)
    }
  }

  "A zoneManagerActor" must {
    "return an empty list after removing created zones" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      zoneManager ! CreateZone(zoneIdentifier2)
      expectMsg(ZoneCreated)
      zoneManager ! RemoveZone(zoneIdentifier)
      expectMsg(ZoneRemoved)
      zoneManager ! RemoveZone(zoneIdentifier2)
      expectMsg(ZoneRemoved)
      zoneManager ! GetZones
      val zones = expectMsgPF() {
        case Zones(zones: List[String]) => zones
      }
      zones shouldBe List()
    }
  }


  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}

