package it.unibo.intelliserra.server

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import it.unibo.intelliserra.core.actuator.{Action, Actuator, OperationalState}
import it.unibo.intelliserra.core.entity.{ActingCapability, SensingCapability}
import it.unibo.intelliserra.core.sensor.{Category, Measure, Sensor}
import it.unibo.intelliserra.device.DeviceDeploy
import it.unibo.intelliserra.server.EntityManager.{JoinActuator, JoinSensor}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.{Await}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
private class DeviceDeployTest extends TestKit(ActorSystem("MySpec"))
 with WordSpecLike
 with BeforeAndAfter
 with BeforeAndAfterAll {

  private var entityManagerActor: TestActorRef[EntityManagerActor] = _
  private implicit val timeout : Timeout = Timeout(5 seconds)
  var deviceDeploy : DeviceDeploy = DeviceDeploy()

  private val sensor:Sensor = new Sensor {
   override def identifier: String = "sensorID"

   override def capability: SensingCapability = SensingCapability(Temperature)

   override def state: Measure = ???
  }

  private val actuator:Actuator = new Actuator {
    override def identifier: String = "actuatorID"

    override def capability: ActingCapability = ActingCapability(Set(DaiAcqua))

    override def state: OperationalState = ???

    override def doAction(action: Action): Unit = ???
  }

  case object Temperature extends Category
  case object DaiAcqua extends Action

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  before {
    entityManagerActor  = TestActorRef.create[EntityManagerActor](system , Props[EntityManagerActor])
  }

  "A deviceDeploy " must {
    "ask for a sensor assignment" in {
      val sensorProbe = TestProbe()
      deploySensor(sensorProbe)
    }
  }

  /*"A deviceDeploy " must {
    "ask for a sensor assignment with an identify that already exists" in {
      val sensorProbe = TestProbe()
      deploySensor(sensorProbe)
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment" in {
      val actuatorProbe = TestProbe()
      deployActuator(actuatorProbe)
    }
  }

  "A deviceDeploy " must {
    "ask for an actuator assignment with an identify that already exists" in {
      val sensorProbe = TestProbe()
      deploySensor(sensorProbe)
    }
  }
*/
  def deploySensor(sensorProbe: TestProbe) : Unit = {
    //entityManagerActor ? JoinSensor(sensor.identifier, sensor.capability, sensorProbe.ref)
    Await.result(deviceDeploy.deploySensor(sensor), timeout.duration)
  }

  def deployActuator(actuatorProbe: TestProbe) : Unit = {
    entityManagerActor ? JoinActuator(actuator.identifier, actuator.capability, actuatorProbe.ref)
    Try(Await.ready(deviceDeploy.deployActuator(actuator), timeout.duration)) match {
      case Success(_) => assert(true)
      case Failure(exception)=> fail(exception)
    }
  }

}
