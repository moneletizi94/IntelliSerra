package it.unibo.intelliserra.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages
import it.unibo.intelliserra.common.communication.Messages.{JoinActuator, JoinOK, JoinSensor}
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.core.actuator._
import it.unibo.intelliserra.core.entity._
import it.unibo.intelliserra.core.sensor._
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.zone.ZoneManagerActor
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
private class GreenHouseControllerSpec extends TestKit(ActorSystem("GreenHouseControllerMySpec"))
  with WordSpecLike
  with BeforeAndAfter
  with TestUtility
  with ImplicitSender
  with BeforeAndAfterAll {

  private var mockZoneID: String = _

  private var greenHouseController: TestActorRef[GreenHouseController] = _
  private var entityManagerActor: ActorRef = _
  private var zoneManagerActor: ActorRef = _
  private var entityRef : ActorRef = _
  private val aggregators: List[Aggregator] = List()

  before {
    this.entityManagerActor = EntityManagerActor()
    this.zoneManagerActor = ZoneManagerActor(aggregators)
    this.greenHouseController = TestActorRef.create(system, Props(new GreenHouseController(zoneManagerActor, entityManagerActor)))
  }

  after {
    killActors(entityManagerActor, zoneManagerActor, greenHouseController)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private val sensor: Sensor = new Sensor {
    override def identifier: String = "sensorID"

    override def capability: SensingCapability = SensingCapability(Temperature)

    override def state: Measure = Measure(IntType(0), Temperature)
  }

  private val sensor2: Sensor = new Sensor {
    override def identifier: String = "sensor2ID"

    override def capability: SensingCapability = SensingCapability(Humidity)

    override def state: Measure = Measure(IntType(0), Temperature)
  }

  private val actuator: Actuator = new Actuator {
    override def identifier: String = "actuatorID"

    override def capability: ActingCapability = ActingCapability(Set(Water))

    override def state: OperationalState = Idle

    override def doAction(action: Action): Unit = {}
  }

  private val actuator2: Actuator = new Actuator {
    override def identifier: String = "actuator2ID"

    override def capability: ActingCapability = ActingCapability(Set(OpenWindow))

    override def state: OperationalState = Idle

    override def doAction(action: Action): Unit = {}
  }

  case object Temperature extends Category

  case object Humidity extends Category

  case object Water extends Action

  case object OpenWindow extends Action

  "A greenHouseController " must {
    "ask for create zone" in {
      mockZoneID = "zone"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
    }
  }

  "A greenHouseController " must {
    "ask for create zone that already exists" in {
      mockZoneID = "zone1"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Conflict))
    }
  }

  "A greenHouseController " must {
    "ask for remove an existing zone" in {
      mockZoneID = "zone2"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(Deleted))
    }
  }

  "A greenHouseController " must {
    "ask to remove a zone that has already been removed" in {
      mockZoneID = "zone3"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(Deleted))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(NotFound, "Zone not found"))
    }
  }

  "A greenHouseController " must {
    "ask to obtain the list of zones" in {
      mockZoneID = "zone4"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! GetZones()
      expectMsg(ServiceResponse(Ok, List(mockZoneID)))
      greenHouseController ! DeleteZone(mockZoneID)
      expectMsg(ServiceResponse(Deleted))
      greenHouseController ! GetZones()
      expectMsg(ServiceResponse(Ok, List()))
    }
  }

  "A greenHouseController " must {
    "ask to assign entity in zone" in {
      mockZoneID = "zone5"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      entityManagerActor ! JoinSensor(sensor.identifier, sensor.capability, SensorActor(sensor))
      expectMsg(JoinOK)
      greenHouseController ! AssignEntity(mockZoneID, sensor.identifier)
      expectMsg(ServiceResponse(Ok))
    }
  }

  "A greenHouseController " must {
    "ask to assign entity in un-existing zone" in {
      mockZoneID = "zone6"
      entityManagerActor ! JoinSensor(sensor2.identifier, sensor2.capability, SensorActor(sensor2))
      expectMsg(JoinOK)
      greenHouseController ! AssignEntity(mockZoneID, sensor2.identifier)
      expectMsg(ServiceResponse(NotFound, "Zone not found"))
    }
  }

  "A greenHouseController " must {
    "ask to assign an already assigned entity" in {
      mockZoneID = "zone7"
      entityRef = ActuatorActor(actuator)
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      entityManagerActor ! JoinActuator(actuator.identifier, actuator.capability, entityRef)
      expectMsg(JoinOK)
      greenHouseController ! AssignEntity(mockZoneID, actuator.identifier)
      expectMsg(ServiceResponse(Ok))
      zoneManagerActor.tell(Messages.Ack, entityRef)
      greenHouseController ! CreateZone("zona8")
      expectMsg(ServiceResponse(Created))
      greenHouseController ! AssignEntity("zona8", actuator.identifier)
      expectMsg(ServiceResponse(Conflict, mockZoneID))
    }
  }

  "A greenHouseController " must {
    "ask to assign an entity that does not exist" in {
      mockZoneID = "zone9"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! AssignEntity(mockZoneID, actuator2.identifier)
      expectMsg(ServiceResponse(NotFound, "Entity not found"))
    }
  }
}
