package it.unibo.intelliserra.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages
import it.unibo.intelliserra.common.communication.Messages.{JoinActuator, JoinOK, JoinSensor}
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.core.actuator._
import it.unibo.intelliserra.core.rule.{Rule, RuleInfo, StatementTestUtils}
import it.unibo.intelliserra.core.sensor._
import it.unibo.intelliserra.device.core.actuator.ActuatorActor
import it.unibo.intelliserra.device.core.sensor.SensorActor
import it.unibo.intelliserra.server.entityManager.EntityManagerActor
import it.unibo.intelliserra.server.rule.RuleEngineService
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
  with BeforeAndAfterAll
  with StatementTestUtils{

  private var mockZoneID: String = _
  private var greenHouseController: TestActorRef[GreenHouseController] = _
  private var entityManagerActor: ActorRef = _
  private var zoneManagerActor: ActorRef = _
  private var ruleEngineServiceActor: ActorRef = _
  private var entityRef: ActorRef = _
  private val ruleID = "rule0"
  private val rule1ID = "rule1"

  before {
    this.zoneManagerActor = ZoneManagerActor(defaultServerConfig.zoneConfig)
    this.entityManagerActor = EntityManagerActor()
    this.ruleEngineServiceActor = RuleEngineService(List(rule))
    this.greenHouseController = TestActorRef.create(system, Props(new GreenHouseController(zoneManagerActor, entityManagerActor, ruleEngineServiceActor)))
  }

  after {
    killActors(entityManagerActor, zoneManagerActor, ruleEngineServiceActor, greenHouseController)
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private val sensor: Sensor = mockSensor("sensorID")
  private val sensor2: Sensor = mockSensor("sensor2ID")
  private val actuator: Actuator = mockActuator("actuatorID")
  private val actuator2: Actuator = mockActuator("actuator2ID")

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
      expectMsg(ServiceResponse(Conflict, "Zone already exists"))
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

  "A greenHouseController " must {
    "ask to dissociate an entity" in {
      mockZoneID = "zone10"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      entityManagerActor ! JoinActuator(actuator.identifier, actuator.capability, entityRef)
      expectMsg(JoinOK)
      greenHouseController ! AssignEntity(mockZoneID, actuator.identifier)
      expectMsg(ServiceResponse(Ok))
      greenHouseController ! DissociateEntity(actuator.identifier)
      expectMsg(ServiceResponse(Ok))
    }
  }

  "A greenHouseController " must {
    "ask to dissociate an already dissociated entity" in {
      mockZoneID = "zone11"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      entityManagerActor ! JoinActuator(actuator2.identifier, actuator2.capability, entityRef)
      expectMsg(JoinOK)
      greenHouseController ! AssignEntity(mockZoneID, actuator2.identifier)
      expectMsg(ServiceResponse(Ok))
      greenHouseController ! DissociateEntity(actuator2.identifier)
      expectMsg(ServiceResponse(Ok))
      greenHouseController ! DissociateEntity(actuator2.identifier)
      expectMsg(ServiceResponse(Error, "Entity already dissociated"))
    }
  }

  "A greenHouseController " must {
    "ask to dissociate an entity that does not exist" in {
      mockZoneID = "zone12"
      greenHouseController ! CreateZone(mockZoneID)
      expectMsg(ServiceResponse(Created))
      greenHouseController ! DissociateEntity(actuator2.identifier)
      expectMsg(ServiceResponse(NotFound, "Entity not found"))
    }
  }

  "A greenHouseController " must {
    "ask to remove entity" in {
      entityManagerActor ! JoinSensor(sensor.identifier, sensor.capability, entityRef)
      expectMsg(JoinOK)
      greenHouseController ! RemoveEntity(sensor.identifier)
      expectMsg(ServiceResponse(Deleted))
    }
  }

  "A greenHouseController " must {
    "ask to remove entity that does not exist" in {
      greenHouseController ! RemoveEntity(sensor2.identifier)
      expectMsg(ServiceResponse(NotFound, "Entity not found"))
    }
  }

  "A greenHouseController " must {
    "take all rules" in {
      greenHouseController ! GetRules
      expectMsg(ServiceResponse(Ok, List(RuleInfo(ruleID, rule))))
    }
  }

  "A greenHouseController " must {
    "ask to enable an existing rule" in {
      greenHouseController ! DisableRule(ruleID)
      expectMsg(ServiceResponse(Ok, "Rule disabled"))
      greenHouseController ! EnableRule(ruleID)
      expectMsg(ServiceResponse(Ok, "Rule enabled"))
    }
  }

  "A greenHouseController " must {
    "ask to to enable a nonexistent rule" in {
      greenHouseController ! EnableRule(rule1ID)
      expectMsg(ServiceResponse(Error, "not possible"))
    }
  }

  "A greenHouseController " must {
    "ask to disable an existing rule" in {
      greenHouseController ! DisableRule(ruleID)
      expectMsg(ServiceResponse(Ok, "Rule disabled"))
    }
  }

  "A greenHouseController " must {
    "ask to to disable a nonexistent rule" in {
      greenHouseController ! DisableRule(rule1ID)
      expectMsg(ServiceResponse(Error, "not possible"))
    }
  }
}

