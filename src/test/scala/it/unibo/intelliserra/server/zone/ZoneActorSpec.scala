package it.unibo.intelliserra.server.zone

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.core.sensor.Category
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ZoneActorSpec extends TestKit(ActorSystem("MyTest")) with TestUtility
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private var zone: TestActorRef[ZoneActor] = _

  before{
    zone = TestActorRef.create(system, Props[ZoneActor])
  }

  "A zoneActor" must {
    "inform its associated entities when it is deleted" in {
      //TODO when associate is ready
      //non riesco a testarlo senza entità a cui mandare il dissociateFromMe
    }
  }

  /*"A zoneActor" must {
    "not be reachable after shutdown" in {
      val testProbe = TestProbe()
      testProbe watch zone
      zone ! DestroyYourself
      testProbe.expectTerminated(zone)
    }
  }

  "A zoneActor" must {
    "have no entity associated just created" in {
      zone.underlyingActor.associatedEntities.isEmpty
    }
  }*/

  "A zoneActor" must {
    "allow you to associate entities that have not been associated with it" in {
      /*val entityActor = TestProbe()
      val registeredSensor = RegisteredSensor("id",SensingCapability(Temperature))
      zone ! AssignEntity(entityActor.ref, registeredSensor)
      entityActor.expectMsg(AssociateToMe(zone))
      zone.tell(Ack, entityActor.ref)
      zone.underlyingActor.associatedEntities.contains(entityActor.ref)
      expectMsg(AssignOk)*/
    }
  }

  case object Temperature extends Category


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
