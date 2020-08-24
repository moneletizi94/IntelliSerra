package it.unibo.intelliserra.server.zone

import akka.actor.{ActorRef, ActorSystem, Terminated}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import it.unibo.intelliserra.common.communication.Protocol._

@RunWith(classOf[JUnitRunner])
class ZoneActorSpec extends TestKit(ActorSystem("MyTest"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private val zoneIdentifier = "Zone1"
  private var zone: ActorRef = _

  before {
    zone = ZoneActor(zoneIdentifier)
  }
  after {
    zone ! DestroyYourself
    expectMsg(Terminated)
  }
  "A zoneActor" must {
    "inform its associated entities when it is deleted" in {
      //TODO when associate is ready
      //non riesco a testarlo senza entità a cui mandare il dissociateFromMe
    }
  }

  "A zoneActor" must {
    "not be reachable after shutdown" in {
      val testProbe = TestProbe()
      testProbe watch zone
      zone ! DestroyYourself
      testProbe.expectTerminated(zone)
    }
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
