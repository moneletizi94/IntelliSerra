package it.unibo.intelliserra.client.core

import it.unibo.intelliserra.core.entity.SensingCapability
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, Sensor}
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.collection.immutable.Stream.Empty

@RunWith(classOf[JUnitRunner])
class ClientSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private val zoneName = "zone1"

  private var client: GreenHouseClient = _
  private var server: GreenHouseServer = _
  private var deviceDeploy: DeviceDeploy = _
  private val aggregators: List[Aggregator] = List()

  private val sensor1: Sensor = new Sensor {
    override def identifier: String = "sensor1"
    override def capability: SensingCapability = SensingCapability(Temperature)
    override def state: Measure = Measure(IntType(0), Temperature)
  }

  private val sensor2: Sensor = new Sensor {
    override def identifier: String = "sensor2"
    override def capability: SensingCapability = SensingCapability(Humidity)
    override def state: Measure = Measure(IntType(0), Humidity)
  }

  case object Temperature extends Category
  case object Humidity extends Category

  before {
    server = GreenHouseServer(GreenhouseName, Hostname, Port)
    client = GreenHouseClient(GreenhouseName, Hostname, Port)
    deviceDeploy = DeviceDeploy(GreenhouseName, Hostname, Port)
    awaitReady(server.start(aggregators))
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

    "be able to assign an entity to an existing zone" in {
      awaitReady(client.createZone(zoneName))
      awaitResult(client.associateEntity(sensor1.identifier, zoneName)) shouldBe zoneName
    }

    "get state from nonexistent zone" in {
      assertThrows[Exception] {
        awaitResult(client.getState(zoneName))
      }
    }

    /*"get state from existing zone" in {
      awaitReady(client.createZone(zoneName))
      awaitResult(client.getState(zoneName)) shouldBe
    }*/
  }
}