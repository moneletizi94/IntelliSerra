package it.unibo.intelliserra.client.core

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.AtomicConditionStatement
import it.unibo.intelliserra.core.rule.dsl.MajorOperator
import it.unibo.intelliserra.core.rule.{Rule, RuleInfo, StatementTestUtils}
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ClientSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility
with StatementTestUtils {

  private val zoneName = "zone1"
  private val zoneName2 = "zone2"

  private var client: GreenHouseClient = _
  private var server: GreenHouseServer = _
  private var deviceDeploy: DeviceDeploy = _
  private val aggregators: List[Aggregator] = List()
  private val actionSet: Set[Action] = Set(Water)
  private val ruleID = "rule0"
  private val rule1ID = "rule1"
  private var rule: Rule = _
  private val notAddedSensor: String = "notAddedSensor"
  private val sensor1: Sensor = mockSensor("sensor1")
  private val sensor2: Sensor = mockSensor("sensor2")
  private val temperatureValue = 20
  private var temperatureStatement: AtomicConditionStatement = _

  before {
    //this.temperatureStatement = AtomicConditionStatement(Temperature, MajorOperator, temperatureValue)
    //this.rule = Rule(temperatureStatement, actionSet)
    server = GreenHouseServer(GreenhouseName, Hostname, Port)
    client = GreenHouseClient(GreenhouseName, Hostname, Port)
    deviceDeploy = DeviceDeploy(GreenhouseName, Hostname, Port)
    awaitReady(server.start(aggregators, List(rule)))
    awaitReady(deviceDeploy.deploySensor(sensor1))
    awaitReady(deviceDeploy.deploySensor(sensor2))
  }

  after {
    awaitReady(server.terminate())
  }

  "A client " should {

    "create a new zone" in {
      awaitResult(client.createZone(zoneName)) shouldBe zoneName
      awaitResult(client.zones()) shouldBe List(zoneName)
    }

    "remove an existing zone" in {
      awaitReady(client.createZone(zoneName))
      awaitResult(client.removeZone(zoneName)) shouldBe zoneName
      awaitResult(client.zones()) shouldBe List()
    }

    "get all available zones" in {
      awaitResult(client.zones()) shouldBe List()
    }

    "fail to create zone if already exist" in {
      awaitResult(client.createZone(zoneName)) shouldBe zoneName
      assertThrows[IllegalArgumentException] {
        awaitResult(client.createZone(zoneName))
      }
    }

    "fail to create zone if server is down" in {
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.createZone(zoneName))
      }
    }

    "fail to remove a non existing zone" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.removeZone(zoneName))
      }
    }

    "fail to remove zone if server is down" in {
      awaitReady(client.createZone(zoneName))
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.removeZone(zoneName))
      }
    }

    "fail to get zone if server is down" in {
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.zones())
      }
    }

    /* --- START TESTING ASSIGN ---*/
    "be able to assign an entity to an existing zone" in {
      awaitReady(client.createZone(zoneName))
      awaitResult(client.associateEntity(sensor1.identifier, zoneName)) shouldBe zoneName
    }

    "fail to assign an entity to a nonexistent zone" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.associateEntity(sensor1.identifier, zoneName))
      }
    }

    "fail to assign a nonexistent entity" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.associateEntity(notAddedSensor, zoneName))
      }
    }

    "fail to assign an entity already assigned" in {
      awaitReady(client.createZone(zoneName))
      awaitReady(client.createZone(zoneName2))
      awaitReady(client.associateEntity(sensor1.identifier, zoneName))
      assertThrows[IllegalArgumentException] {
        awaitResult(client.associateEntity(sensor1.identifier, zoneName2))
        awaitResult(client.associateEntity(sensor1.identifier, zoneName2))
      }
    }
    /* --- END TESTING ASSIGN ---*/

    /* --- START TEST ON REMOVE ENTITY --- */
    "be able to remove an existing entity" in {
      awaitReady(client.createZone(zoneName))
      awaitReady(client.associateEntity(sensor1.identifier, zoneName))
      awaitResult(client.removeEntity(sensor1.identifier)) shouldBe sensor1.identifier
    }
    "fail to remove a nonexistent entity" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.removeEntity(notAddedSensor))
      }
    }
    /* --- END TEST ON REMOVE ENTITY --- */

    /* --- START TEST ON DISSOCIATE ENTITY --- */
    "be able to dissociate an associated entity" in {
      awaitReady(client.createZone(zoneName))
      awaitReady(client.associateEntity(sensor1.identifier, zoneName))
      awaitResult(client.dissociateEntity(sensor1.identifier)) shouldBe sensor1.identifier
    }
    "fail to dissociate a non-associated entity" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.dissociateEntity(sensor1.identifier))
      }
    }
    "fail to dissociate a nonexistent entity" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.dissociateEntity(notAddedSensor))
      }
    }
    /* --- END TEST ON DISSOCIATE ENTITY --- */

    "get state from nonexistent zone" in {
      assertThrows[Exception] {
        awaitResult(client.getState(zoneName))
      }
    }

    "get state from existing zone" in {
      awaitReady(client.createZone(zoneName))
      awaitResult(client.getState(zoneName)) shouldBe None
    }

    /*--- START TEST RULES ---*/
    "get all rules" in {
      awaitResult(client.getRules) shouldBe List(RuleInfo(ruleID, _))
    }

    "enable an existing rule" in {
      awaitResult(client.enableRule(ruleID)) shouldBe "Rule enabled"
    }

    "not enable a nonexistent rule" in {
      assertThrows[Exception] {
        awaitResult(client.enableRule(rule1ID))
      }
    }

    "disable an existing rule" in {
      awaitResult(client.disableRule(ruleID)) shouldBe "Rule disabled"
    }

    "not disable a nonexistent rule" in {
      assertThrows[Exception] {
        awaitResult(client.disableRule(rule1ID))
      }
    }
    /*--- END TEST RULES ---*/

  }
}