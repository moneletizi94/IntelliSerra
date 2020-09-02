package it.unibo.intelliserra.server.zone

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages.{AddEntity, DeleteEntity, GetState, MyState, SensorMeasure}
import it.unibo.intelliserra.core.entity.{EntityChannel, RegisteredSensor, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, Measure}
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import it.unibo.intelliserra.server.aggregation.Aggregator._
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation._

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class ZoneActorSpec extends TestKit(ActorSystem("MyTest")) with TestUtility
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private var zone: TestActorRef[ZoneActor] = _
  private val registeredSensor = RegisteredSensor("sensorId", SensingCapability(Temperature))
  private val aggregators = List(createAggregator(Temperature)(sum),
                                  createAggregator(Weather)(moreFrequent))

  before{
    zone = TestActorRef.create(system, Props(new ZoneActor(aggregators)))
  }

  "A zoneActor" must {
    "have no entity associated just created" in {
      zone.underlyingActor.associatedEntities.isEmpty
    }
  }

  "A zoneActor" must {
    "allow you to associate entities that have not been associated with it" in {
      val sensorProbe = TestProbe()
      val entityChannel = EntityChannel(registeredSensor, sensorProbe.ref)
      zone ! AddEntity(entityChannel)
      zone.underlyingActor.associatedEntities.contains(entityChannel) shouldBe true
    }
  }

  "A zoneActor" must {
    "allow you to remove entities that have been associated with it" in {
      val sensorProbe = TestProbe()
      val entityChannel = EntityChannel(registeredSensor, sensorProbe.ref)
      zone ! DeleteEntity(entityChannel)
      zone.underlyingActor.associatedEntities.contains(entityChannel) shouldBe false
    }
  }

  "A zoneActor" should {
    "sends its state after a request of it" in {
      val testProbe = TestProbe()
      zone.tell(GetState,testProbe.ref)
      testProbe.expectMsgType[MyState]
    }
  }

  "A zoneActor" should {
    "preserve only last measure sent by the same sensor" in {
      val sensor = TestProbe()
      val measure1 = Measure(Temperature)(27)
      zone tell(SensorMeasure(measure1), sensor.ref)
      val measure2 = Measure(Temperature)(20)
      zone tell(SensorMeasure(measure2), sensor.ref)
      zone.underlyingActor.sensorsValue(sensor.ref) shouldBe measure2
      zone.underlyingActor.sensorsValue(sensor.ref) should not be measure1
    }
  }

  "A zoneActor" should {
    "compute sensor value aggregation correctly" in {
      sendNMessageFromNProbe(10, zone, SensorMeasure(Measure(Temperature)(1)))
      zone.underlyingActor.computeAggregatedPerceptions() shouldBe List(Measure(Temperature)(10))
    }
  }

  "A zone " must {
    " have at most one aggregator for each category when created" in {
      assertThrows[IllegalArgumentException] {
        ZoneActor("zoneName", aggregators.+:(createAggregator(Temperature)(min)))
      }
    }
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

}