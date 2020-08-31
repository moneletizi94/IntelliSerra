package it.unibo.intelliserra.device.core

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom}
import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState}
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor}
import it.unibo.intelliserra.device.core.actuator.ActuatorActor
import it.unibo.intelliserra.device.core.sensor.SensorActor
import it.unibo.intelliserra.utils.TestUtility
import monix.reactive.Observable
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class EntityActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var sensor: TestActorRef[SensorActor] = _
  private var actuator: TestActorRef[ActuatorActor] = _
  private var zoneManagerProbe: TestProbe = _
  private var zoneProbe: TestProbe = _
  private val zoneID = "ZONE1"

  private val mockSensor = new Sensor {
    case object Temperature extends Category
    override def identifier: String = ""
    override def capability: SensingCapability = SensingCapability(Temperature)
    override def measures: Observable[Measure] = Observable()
  }

  private val mockActuator = new Actuator {
    case object DoWater extends Action
    override def identifier: String = ""
    override def capability: ActingCapability = ActingCapability(Set(DoWater))
    override def state: Observable[OperationalState] = Observable()
    override def actionHandler: ActionHandler = { case _ => Future.successful(Idle) }
  }

  before {
    zoneManagerProbe = TestProbe()
    zoneProbe = TestProbe()
  }

  after {
    killActors(sensor, actuator)
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "A sensor " must {
    "send an ack to confirm association" in {
      sensor = TestActorRef.create(system, Props(new SensorActor(mockSensor)))
      sensor.tell(AssociateTo(zoneProbe.ref, zoneID), zoneManagerProbe.ref)
      zoneManagerProbe.expectMsg(Ack)
      sensor.underlyingActor.zone.contains(zoneManagerProbe.ref)
    }

    "dissociate from a zone" in {
      sensor = TestActorRef.create(system, Props(new SensorActor(mockSensor)))
      sensor.tell(DissociateFrom(zoneProbe.ref, zoneID), zoneManagerProbe.ref)
      sensor.underlyingActor.zone.isEmpty shouldBe true
    }
  }

  "An actuator " must {
    "send an ack to confirm association" in {
      actuator = TestActorRef.create(system, Props(new ActuatorActor(mockActuator)))
      actuator.tell(AssociateTo(zoneProbe.ref, zoneID), zoneManagerProbe.ref)
      zoneManagerProbe.expectMsg(Ack)
      actuator.underlyingActor.zone.contains(zoneManagerProbe.ref)
    }

    "dissociate from a zone" in {
      actuator = TestActorRef.create(system, Props(new ActuatorActor(mockActuator)))
      actuator.tell(DissociateFrom(zoneProbe.ref, zoneID), zoneManagerProbe.ref)
      actuator.underlyingActor.zone.isEmpty shouldBe true
    }
  }
}
