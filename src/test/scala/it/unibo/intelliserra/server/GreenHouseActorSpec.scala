package it.unibo.intelliserra.server

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.server.core.GreenHouseActor
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GreenHouseActorSpec extends TestKit(ActorSystem("test", GreenHouseConfig()))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private val GREENHOUSE_NAME = "serra1"
  private val serverActor: ActorRef = GreenHouseActor(GREENHOUSE_NAME)

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A greenhouse actor" must {
    "send Started message when is successfully started" in {
      serverActor ! Start
      expectMsg(Started)
    }

    "send a ServerError if is already running" in {
      serverActor ! Start
      expectMsgType[ServerError]
    }
  }
}
