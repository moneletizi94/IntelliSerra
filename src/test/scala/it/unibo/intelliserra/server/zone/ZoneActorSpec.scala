package it.unibo.intelliserra.server.zone

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.core.actuator.{DoingActions, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.entity.{EntityChannel, RegisteredActuator, RegisteredEntity, RegisteredSensor}
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation.Aggregator._
import it.unibo.intelliserra.server.aggregation._
import it.unibo.intelliserra.server.zone.ZoneActor.ComputeState
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, Light, Water}
import it.unibo.intelliserra.utils.TestUtility.Categories.{Temperature, Weather}
import it.unibo.intelliserra.utils.{Generator, Sample, TestUtility}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration._

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
    zone = TestActorRef.create(system, ZoneActor.props(aggregators,1 seconds))
  }

  "A zoneActor" must {
    "have no entity associated just created" in {
      zone.underlyingActor.associatedEntities.isEmpty
    }
  }

  "A zoneActor" must {
    "allow you to associate entities that have not been associated with it" in {
      val addedEntity = addEntity(registeredSensor)
      zone.underlyingActor.associatedEntities.contains(EntityChannel(registeredSensor, addedEntity)) shouldBe true
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
      zone tell(SensorMeasureUpdated(measure1), sensor.ref)
      val measure2 = Measure(Temperature)(20)
      zone tell(SensorMeasureUpdated(measure2), sensor.ref)
      zone.underlyingActor.sensorsValue(sensor.ref) shouldBe measure2
      zone.underlyingActor.sensorsValue(sensor.ref) should not be measure1
    }
  }

  "A zoneActor" should {
    "update the state of the actuator correctly" in {
      val actuator = TestProbe()
      val operationalState = Idle
      zone tell(ActuatorStateChanged(operationalState), actuator.ref)
      val operationalState2 = OperationalState(Water)
      zone tell(ActuatorStateChanged(operationalState2), actuator.ref)
      zone.underlyingActor.actuatorsState(actuator.ref) shouldBe operationalState2
      zone.underlyingActor.actuatorsState(actuator.ref) should not be operationalState
      //checkReplace(zone.underlyingActor.actuatorsState)
    }
  }

  private def sendMessageAndCheckReplace[T](map : Map[ActorRef,T])(implicit sample : Sample[T], system: ActorSystem) = {
    val sender = TestProbe()
    val sendingValue1 = Generator.generate(sample)
    zone tell(sendingValue1, sender.ref)
    val sensingValue2 = Generator.generate
    zone tell(sensingValue2, sender.ref)
    map(sender.ref) shouldBe sensingValue2
    map(sender.ref) should not be sendingValue1
  }

  "A zoneActor" should {
    "compute sensor value aggregation correctly" in {
      sendNMessageFromNProbe(10, zone, SensorMeasureUpdated(Measure(Temperature)(1)))
      zone.underlyingActor.computeAggregatedPerceptions() shouldBe List(Measure(Temperature)(10))
    }
  }

  "A zoneActor" should {
    "compute actuators state correctly" in {
      sendNMessageFromNProbe(5, zone, ActuatorStateChanged(Idle))
      sendNMessageFromNProbe(3, zone, ActuatorStateChanged(OperationalState(Water)))
      sendNMessageFromNProbe(2, zone, ActuatorStateChanged(OperationalState(Fan)))
      zone.underlyingActor.computeActuatorState().diff(List(Fan,Water)) shouldBe List()
    }
  }

  "A zone " must {
    " have at most one aggregator for each category when created" in {
      assertThrows[IllegalArgumentException] {
        ZoneActor("zoneName", aggregators.+:(createAggregator(Temperature)(min)))
      }
    }
  }

  "A zone with period of 1 seconds" should  {
    "have no state in 1 seconds after creation" in {
      zone ! GetState
      expectMsg(MyState(None))
    }
  }


  "A zone " should  {
    "compute its state after receiving computeState" in {
      val probe = TestProbe()
      zone.tell(SensorMeasureUpdated(Measure(Temperature)(10)),probe.ref)
      zone ! ComputeState
      zone.underlyingActor.state shouldBe Option(State(List(Measure(Temperature)(10)),List()))
    }
  }

  "A zone " should {
    "send action to its actuator according to theirs capabilities" in {
      val sensor1 = addEntity(registeredSensor)
      val actuator1 = addEntity(RegisteredActuator("act1", ActingCapability(Set(Water.getClass, Fan.getClass))))
      val actuator2 = addEntity(RegisteredActuator("act2", ActingCapability(Set(Water.getClass))))
      val actuator3 = addEntity(RegisteredActuator("act3", ActingCapability(Set(Light.getClass))))
      zone ! DoActions(Set(Water,Fan))
      sensor1.expectNoMessage(1 seconds)
      actuator3.expectNoMessage(1 seconds)
      actuator1.expectMsg(DoActions(Set(Water, Fan)))
      actuator2.expectMsg(DoActions(Set(Water)))
    }
  }

  private def addEntity(entity : RegisteredEntity): TestProbe = {
    val entityProbe = TestProbe()
    val entityChannel = EntityChannel(entity, entityProbe)
    zone ! AddEntity(entityChannel)
    entityProbe
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

}