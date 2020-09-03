package it.unibo.intelliserra.server.core

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}
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

  private var serverActor: TestActorRef[GreenHouseActor] = TestActorRef.create(system, Props[GreenHouseActor]())
  private val aggregators: List[Aggregator] = List()

  before {
    serverActor = TestActorRef.create(system, Props[GreenHouseActor]())
  }

  after {
    killActors(serverActor,
      serverActor.underlyingActor.entityManagerActor,
      serverActor.underlyingActor.zoneManagerActor,
      serverActor.underlyingActor.greenHouseController)
  }


  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A greenhouse actor" must {
    "send Started message when is successfully started" in {
      serverActor ! Start(aggregators)
      expectMsg(Started)
    }

    "send a ServerError if is already running" in {
      serverActor ! Start(aggregators)
      expectMsg(Started)
      serverActor ! Start(aggregators)
      expectMsgType[ServerError]
    }
  }
}
