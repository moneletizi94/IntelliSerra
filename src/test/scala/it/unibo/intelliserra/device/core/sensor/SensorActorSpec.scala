package it.unibo.intelliserra.device.core.sensor

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.SensingCapability
import it.unibo.intelliserra.core.sensor.{Category, DoubleType, Measure, Sensor, ValueType}
import it.unibo.intelliserra.device.core.sensor.SensorActor.SensorMeasureUpdated
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class SensorActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private val SensorPeriod = 2 seconds
  private val SensorCapability = Capability.sensing(Temperature)
  private val NotSupportedCategory = Humidity
  private val MeasureStream = Stream.continually(Measure(Temperature)(Random.nextInt(100)))
  private var sensor: SensorCallbackTestable = _
  private var sensorActor: TestActorRef[SensorActor] = _
  private var initCallbackPromise: Promise[Boolean] = _

  before {
    initCallbackPromise = Promise()
    sensor = new SensorCallbackTestable("sensor1", SensorPeriod, SensorCapability, MeasureStream)
    sensor.addInit(initCallbackPromise)
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

    "handle init event when actor start" in {
      awaitResult(initCallbackPromise.future)
    }

    "handle association event when is associated to zone from server" in {
      val callbackPromise = Promise[Boolean]()
      sensor.addAssociatePromise(callbackPromise)
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      awaitResult(callbackPromise.future)
    }

    "handle dissociation event when is associated to zone from server" in {
      val callbackPromise = Promise[Boolean]()
      sensor.addDissociatePromise(callbackPromise)
      sensorActor.tell(DissociateFrom(testActor, testActorName), testActor)
      awaitResult(callbackPromise.future)
    }

    "handle periodic measure sampling after zone association" in {
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      expectMsgType[SensorMeasureUpdated](SensorPeriod * 2)
    }

    "send only measure with category declared in capability" in {
      sensor.defineMeasureStream(Stream.continually(Measure(NotSupportedCategory)(0)))
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      expectNoMessage(SensorPeriod * 2)
    }

    "stop to handle periodic measure sampling when dissociated" in {
      sensorActor.tell(AssociateTo(testActor, testActorName), testActor)
      expectMsg(Ack)
      expectMsgType[SensorMeasureUpdated](SensorPeriod * 2)
      sensorActor.tell(DissociateFrom(testActor, testActorName), testActor)
      expectNoMessage(SensorPeriod * 2)
    }
  }

  private class SensorCallbackTestable(override val identifier: String,
                                       override val readPeriod: FiniteDuration,
                                       override val capability: SensingCapability,
                                       private var measures: Stream[Measure]) extends Sensor {
    case object OnInit
    case object OnAssociateZone
    case object OnDissociateZone

    private var callbackPromise: Map[Any, Promise[Boolean]] = Map()

    def defineMeasureStream(measures: Stream[Measure]): Unit = this.measures = measures
    def addInit(promise: Promise[Boolean]): Unit = callbackPromise += (OnInit -> promise)
    def addAssociatePromise(promise: Promise[Boolean]): Unit = callbackPromise += (OnAssociateZone -> promise)
    def addDissociatePromise(promise: Promise[Boolean]): Unit = callbackPromise += (OnDissociateZone -> promise)

    override def read(): Option[Measure] = Option(measures.iterator.next())
    override def onInit(): Unit = callbackPromise.get(OnInit).foreach(_.success(true))
    override def onAssociateZone(zoneName: String): Unit = callbackPromise.get(OnAssociateZone).foreach(_.success(true))
    override def onDissociateZone(zoneName: String): Unit = callbackPromise.get(OnDissociateZone).foreach(_.success(true))
  }


}
