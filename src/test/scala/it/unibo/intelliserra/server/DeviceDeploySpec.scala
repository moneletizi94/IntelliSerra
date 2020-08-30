package it.unibo.intelliserra.server

import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, Sensor}
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.{Await}
import scala.util.{Failure, Success, Try}


@RunWith(classOf[JUnitRunner])
private class DeviceDeploySpec extends WordSpecLike
  with BeforeAndAfter
  with TestUtility {

  private var server: GreenHouseServer = _
  private var deviceDeploy: DeviceDeploy = _

  before {
    this.server = GreenHouseServer(GreenhouseName, Hostname, Port)
    this.deviceDeploy = DeviceDeploy(GreenhouseName, Hostname, Port)
    awaitReady(this.server.start())
  }

  after {
    awaitReady(this.server.terminate())
  }

  private val sensor:Sensor = new Sensor {
   override def identifier: String = "sensorID"
   override def capability: SensingCapability = SensingCapability(Temperature)
   override def state: Measure = Measure(IntType(0), Temperature)
  }

  private val sensor2:Sensor = new Sensor {
    override def identifier: String = "sensor2ID"
    override def capability: SensingCapability = SensingCapability(Humidity)
    override def state: Measure = Measure(IntType(0), Temperature)
  }

  private val actuator:Actuator = new Actuator {
    override def identifier: String = "actuatorID"
    override def capability: ActingCapability = ActingCapability(Set(Water))
    override def state: OperationalState = Idle
    override def doAction(action: Action): Unit = {}
  }

  private val actuator2:Actuator = new Actuator {
    override def identifier: String = "actuator2ID"
    override def capability: ActingCapability = ActingCapability(Set(OpenWindow))
    override def state: OperationalState = Idle
    override def doAction(action: Action): Unit = {}
  }

  case object Temperature extends Category
  case object Humidity extends Category
  case object Water extends Action
  case object OpenWindow extends Action

  "A deviceDeploy " must {
    "ask for a sensor assignment" in {
      Try(awaitReady(deviceDeploy.deploySensor(sensor))) match {
        case Success(_) => succeed
        case Failure(exception)=> fail(exception)
      }
    }
  }

  "A deviceDeploy ask for a sensor assignment with an identify that already exists" in {
      Try(awaitReady(deviceDeploy.deploySensor(sensor2))) match {
        case Success(_) =>
          Try(awaitReady(deviceDeploy.deploySensor(sensor2))) match {
            case Success(_) => fail()
            case Failure(_) => succeed
          }
        case Failure(exception) => fail(exception)
      }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment" in {
      Try(Await.ready(deviceDeploy.deployActuator(actuator), timeout.duration)) match {
        case Success(_) => succeed
        case Failure(exception)=> fail(exception)
      }
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment with an identify that already exists" in {
      Try(awaitReady(deviceDeploy.deployActuator(actuator2))) match {
        case Success(_) =>
          Try(awaitReady(deviceDeploy.deployActuator(actuator2))) match {
            case Success(_) => fail()
            case Failure(_) => succeed
          }
        case Failure(exception) => fail(exception)
      }
    }
  }
}
