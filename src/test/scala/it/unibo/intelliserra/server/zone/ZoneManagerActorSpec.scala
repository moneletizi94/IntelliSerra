package it.unibo.intelliserra.server.zone

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import it.unibo.intelliserra.server.zone.ZoneManagerActor.{CreateZone, NoZone, Zone, ZoneCreationError, ZoneCreationOk, ZoneExists}
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
  private val zoneIdentifierNotAdded = "NoZone"
  private val zoneManager: ActorRef = ZoneManagerActor()

  "A zoneManagerActor" must {
    "accept the creation of a zone with a never-used identifier" in {
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreationOk)
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
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}

