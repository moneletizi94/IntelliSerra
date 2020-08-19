package it.unibo.intelliserra.examples

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.examples.ExampleZoneActor.{AssignCompleted, AssignSensor, AssignToMe}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ExampleZoneActorTest extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll  {

  var zone: TestActorRef[ExampleZoneActor] = _

  before {
    zone  = TestActorRef.create[ExampleZoneActor](system, Props[ExampleZoneActor])
  }

  "A zone actor " must {
    " send assignment to sensor" in {
      val sensorProbe = TestProbe()
      zone ! AssignSensor(sensorProbe.ref)

      sensorProbe.expectMsg(AssignToMe(zone))
      sensorProbe.forward(zone, AssignCompleted(sensorProbe.ref))

      zone.underlyingActor.sensors shouldBe List(sensorProbe.ref)
    }
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
