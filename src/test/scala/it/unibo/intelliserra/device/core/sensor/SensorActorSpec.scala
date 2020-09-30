package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom, SensorMeasureUpdated}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.perception.{IntType, Measure}
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration._
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class SensorActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private val SensorName = "MockSensor"
  private val SensorPeriod = 2 seconds
  private val SensorCategory = Temperature
  private val NotSupportedCategory = Humidity
  private val MeasureStream = Stream.continually(IntType(Random.nextInt(100)))
  private var sensor: Sensor = _

  private var sensorActor: TestActorRef[SensorActor] = _

  before {
    sensor = Sensor(SensorName, SensorCategory, SensorPeriod)(MeasureStream)
    sensorActor = TestActorRef.create(system, SensorActor.props(sensor))
  }

  after {
    sensorActor ! PoisonPill
  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  "A sensor" must {

    "send an ack when is associated to zone from server" in {
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
    }

    "handle periodic measure sampling after zone association" in {
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      expectMsgType[SensorMeasureUpdated](SensorPeriod * 2)
    }

    "send only measure with category declared in capability" in {
      val wrongSensor = WrongSensor()
      val wrongSensorActor = SensorActor(wrongSensor)
      wrongSensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      expectNoMessage(SensorPeriod * 2)
      wrongSensorActor ! PoisonPill
    }

    "stop to handle periodic measure sampling when dissociated" in {
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      expectMsgType[SensorMeasureUpdated](SensorPeriod * 2)
      sensorActor.tell(DissociateFrom(testActor, testActorName), testActor)
      expectNoMessage(SensorPeriod * 2)
    }
  }

  private case class WrongSensor() extends Sensor {
    override def sensePeriod: FiniteDuration = SensorPeriod
    override protected def read(): Measure = Measure(NotSupportedCategory)(0)
    override def identifier: String = SensorName
    override def capability: Capability = Capability.sensing(SensorCategory)
  }
}
