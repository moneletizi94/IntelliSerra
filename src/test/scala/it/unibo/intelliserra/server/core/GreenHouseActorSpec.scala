package it.unibo.intelliserra.server.core

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Protocol.{GetZones, ServiceResponse}
import it.unibo.intelliserra.server.core.GreenHouseActor._
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GreenHouseActorSpec extends TestKit(ActorSystem("test", GreenHouseConfig()))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var serverActor: TestActorRef[GreenHouseActor] = _

  before {
    serverActor = TestActorRef.create(system, Props(new GreenHouseActor(defaultServerConfig.ruleConfig, defaultServerConfig.zoneConfig)))
  }

  after {
    serverActor ! Stop
    expectMsg(Stopped)
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A greenhouse actor" must {
    "send Started message when is successfully started" in {
      serverActor ! Start
      expectMsg(Started)
    }

    "send a ServerError if is already running" in {
      serverActor ! Start
      expectMsg(Started)
      serverActor ! Start
      expectMsgType[ServerError]
    }

    "send Stopped message when is successfully stopped" in {
      serverActor ! Start
      expectMsg(Started)
      serverActor ! Stop
      expectMsg(Stopped)
    }

    "send a ServerError if is already stopped" in {
      serverActor ! Stop
      expectMsgType[ServerError]
    }

    "accept request if started" in {
      serverActor ! Start
      expectMsg(Started)
      makeTestRequest()
      expectMsgType[ServiceResponse]
    }

    "ignore request if stopped" in {
      awaitReady(serverActor ? Start)
      awaitReady(serverActor ? Stop)

      makeTestRequest()
      expectNoMessage()
    }
  }

  private def makeTestRequest(): Unit = {
    serverActor ! GetZones()
  }
}
