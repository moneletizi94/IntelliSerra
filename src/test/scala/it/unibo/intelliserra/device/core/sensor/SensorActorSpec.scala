package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo}
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SensorActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var sensor: Sensor = _
  private var sensorActor: TestActorRef[SensorActor] = _

  before {

    sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
    expectMsg(Ack)
  }

  after {
    sensorActor ! PoisonPill
  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  "A sensor" must {

    "" in {


    }
  }

}
