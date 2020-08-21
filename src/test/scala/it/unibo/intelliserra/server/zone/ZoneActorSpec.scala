package it.unibo.intelliserra.server.zone

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ZoneActorSpec extends TestKit(ActorSystem("MyTest"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private val zoneIdentifier = "Zone1"
  private val zoneIdentifierNotAdded = "FakeZone"
  private val zone: ActorRef = ZoneActor(zoneIdentifier)

  "A zoneActor" must {
    "inform its sensor when it is deleted" in {

    }
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
