package it.unibo.intelliserra.server

import it.unibo.intelliserra.core.actuator.Actuator
import it.unibo.intelliserra.core.sensor.Sensor
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.Await
import scala.util.{Failure, Success, Try}


@RunWith(classOf[JUnitRunner])
private class DeviceDeploySpec extends WordSpecLike
  with BeforeAndAfter
  with TestUtility {

  private var server: GreenHouseServer = _
  private var deviceDeploy: DeviceDeploy = _
  private val aggregators: List[Aggregator] = List()

  before {
    this.server = GreenHouseServer(GreenhouseName, Hostname, Port)
    this.deviceDeploy = DeviceDeploy(GreenhouseName, Hostname, Port)
    awaitReady(this.server.start(aggregators, List()))
  }

  after {
    awaitReady(this.server.terminate())
  }

  private val sensor:Sensor = mockSensor("sensor")
  private val sensor2:Sensor = mockSensor("sensor2")
  private val actuator:Actuator = mockActuator("actuator")
  private val actuator2:Actuator = mockActuator("actuator2")

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
