package it.unibo.intelliserra.server

import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.device.core.actuator.Actuator
import it.unibo.intelliserra.device.core.sensor.Sensor
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{AsyncWordSpecLike, BeforeAndAfter}
import org.scalatestplus.junit.JUnitRunner

import scala.util.{Failure, Success}


@RunWith(classOf[JUnitRunner])
private class DeviceDeploySpec extends AsyncWordSpecLike
  with BeforeAndAfter
  with TestUtility {

  private var server: GreenHouseServer = _
  private var deviceDeploy: DeviceDeploy = _
  private var sensor: Sensor = _
  private var sensor2: Sensor = _
  private var actuator: Actuator = _
  private var actuator2: Actuator = _

  before {
    this.server = GreenHouseServer(defaultServerConfig)
    this.deviceDeploy = DeviceDeploy(GreenhouseName, Hostname, Port)
    awaitReady(this.server.start())
    sensor = mockTemperatureSensor("sensor")
    sensor2 = mockTemperatureSensor("sensor2")
    actuator = mockActuator("actuator")
    actuator2 = mockActuator("actuator2")
  }

  after {
    awaitReady(this.server.terminate())
  }

  "A deviceDeploy " must {
    "ask for a sensor assignment" in {
      deviceDeploy.join(sensor).transform {
        case Success(_) => Success(succeed)
        case Failure(exception) => fail(exception)
      }
    }
  }

  "A deviceDeploy" must {
    "ask for a sensor assignment with an identify that already exists" in {
      awaitReady(deviceDeploy.join(sensor2))
      deviceDeploy.join(sensor2).transform {
        case Success(_) => Failure(fail())
        case Failure(_) => Success(succeed)
      }
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment" in {
      deviceDeploy.join(actuator).transform {
        case Success(_) => Success(succeed)
        case Failure(exception) => fail(exception)
      }
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment with an identify that already exists" in {
      awaitReady(deviceDeploy.join(actuator2))
      deviceDeploy.join(actuator2).transform {
        case Success(_) => Failure(fail())
        case Failure(_) => Success(succeed)
      }
    }
  }

}
