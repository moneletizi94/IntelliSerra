package it.unibo.intelliserra.server.zone

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{RemoveZone, ZoneCreated, ZoneRemoved}
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
  private val zoneManager: ActorRef = ZoneManagerActor()
/*
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

  "A zoneManagerActor" must {
    "accept the creation of a zone with a never-used identifier" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
    }
  }

  "A zoneManagerActor" must {
    "refuse the creation of a zone with a yet-used identifier" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreationError)
    }
  }

  "A zoneManagerActor" must {
    "memorize a just created zone" in {
      zoneManager ! ZoneExists(zoneIdentifier)
      expectMsgType[Zone]
    }
  }

  "A zoneManagerActor" should {
    "not have an unexisting identifier" in {
      zoneManager ! ZoneExists(zoneIdentifierNotAdded)
      expectMsg(NoZone)
    }
  }

  "A zoneManagerActor" must {
    "delete an existing zone when requested" in {
      zoneManager ! RemoveZone(zoneIdentifier)
      expectMsg(ZoneRemoved)
      zoneManager ! ZoneExists(zoneIdentifier)
      expectMsg(NoZone)
      /*var zone = expectMsgPF() {
        case Zone(zoneRef: ActorRef) => zoneRef
      }*/
    }
  }

  "A zoneManagerActor" must {
    "refuse to delete an unexisting zone when requested" in {
      zoneManager ! RemoveZone(zoneIdentifierNotAdded)
      expectMsg(NoZone)
    }
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }*/
}

