package it.unibo.intelliserra.server.zone

import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.core.entity.{EntityChannel, RegisteredSensor, SensingCapability}
import it.unibo.intelliserra.core.sensor.Category
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ZoneManagerActorSpec extends TestKit(ActorSystem("MyTest"))
  with TestUtility
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  private val zoneIdentifier = "Zone1"
  private val zoneIdentifier2 = "Zone2"
  private val zoneIdentifierNotAdded = "FakeZone"
  private var zoneManager: TestActorRef[ZoneManagerActor] = _

  before  {
    zoneManager = TestActorRef.create(system, Props[ZoneManagerActor])
  }
  after {
    killActors(zoneManager.underlyingActor.zones.values.toSeq:_*)
  }
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A zoneManagerActor" should {
    /* --- START TEST ON CREATE --- */
    "allow to create a zone with a never-used identifier" in {
      createZonesAndExpectMsg(zoneIdentifier)
      zoneManager.underlyingActor.zones.contains(zoneIdentifier) shouldBe true
      zoneManager.underlyingActor.assignedEntities.contains(zoneIdentifier) shouldBe true
    }

    "refuse the creation of a zone with a yet-used identifier" in {
      createZonesAndExpectMsg(zoneIdentifier)
      zoneManager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneAlreadyExists)
    }
    /* --- END TEST ON CREATE --- */

    /* --- START TEST ON GET --- */
    "return an empty list when it hasn't zones" in {
      zoneManager ! GetZones
      val zones = expectMsgPF() {
        case ZonesResult(zones: List[String]) => zones
      }
      zones shouldBe List()
    }

    "return a list containing created zones" in {
      createZonesAndExpectMsg(zoneIdentifier, zoneIdentifier2)
      zoneManager ! GetZones
      val zones = expectMsgPF() {
        case ZonesResult(zones: List[String]) => zones
      }
      zones shouldBe List(zoneIdentifier, zoneIdentifier2)
    }
    /* --- END TEST ON GET --- */

    /* --- START TEST ON REMOVE --- */
    "update info on its structures after removing created zones" in {
      createZonesAndExpectMsg(zoneIdentifier, zoneIdentifier2)
      deleteZonesAndExpectMsg(zoneIdentifier, zoneIdentifier2)
      zoneManager ! GetZones
      val zones = expectMsgPF() {
        case ZonesResult(zones: List[String]) => zones
      }
      zones shouldBe List()
      zoneManager.underlyingActor.assignedEntities.contains(zoneIdentifier) shouldBe false
      zoneManager.underlyingActor.pending.contains(zoneIdentifier) shouldBe false
      zoneManager.underlyingActor.assignedEntities.contains(zoneIdentifier2) shouldBe false
      zoneManager.underlyingActor.pending.contains(zoneIdentifier2) shouldBe false
    }

    "refuse to delete a nonexistent zone when requested" in {
      zoneManager ! RemoveZone(zoneIdentifierNotAdded)
      expectMsg(ZoneNotFound)
    }

    "kill a zoneActor when removing a zone" in {
      val probe = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier)
      val zoneRef: ActorRef = zoneManager.underlyingActor.zones(zoneIdentifier)
      probe watch zoneRef
      deleteZonesAndExpectMsg(zoneIdentifier)
      probe.expectMsgType[Terminated]
    }

    "inform associated and pending entities when removing their zone" in {
      val entityProbePending = TestProbe()
      val entityProbeAssigned = TestProbe()
      val entityProbeOtherZone = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier)
      createZonesAndExpectMsg(zoneIdentifier2)

      informAndExpectMsgOnAssign(zoneManager, entityProbePending, zoneIdentifier)
      informAndExpectMsgOnAssign(zoneManager, entityProbeAssigned, zoneIdentifier)
      informAndExpectMsgOnAssign(zoneManager, entityProbeOtherZone, zoneIdentifier2)

      deleteZonesAndExpectMsg(zoneIdentifier)
      entityProbeAssigned.expectMsgType[DissociateFrom]
      entityProbePending.expectMsgType[DissociateFrom]
      entityProbeOtherZone.expectNoMessage
    }
    /* --- END TEST ON REMOVE --- */

    /* --- START TEST ON ASSIGN --- */
    "refuse to assign an entity to a nonexistent zone" in {
      val entityProbe = TestProbe()
      zoneManager ! AssignEntityToZone(zoneIdentifierNotAdded, entityChannelWithRef(entityProbe.ref))
      expectMsg(ZoneNotFound)
    }

    "inform an unassigned entity to assign to a zone" in {
      val entityProbe = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier)
      informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier)
    }

    "accept to assign an entity not assigned to an existing zone" in {
      val entityProbe = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier)
      zoneManager.underlyingActor.pending.contains(zoneIdentifier) shouldBe false
      val entityChannel = informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier)
      zoneManager.underlyingActor.pending.contains(zoneIdentifier) shouldBe true
      zoneManager.underlyingActor.pending(zoneIdentifier).contains(entityChannel) shouldBe true
      zoneManager.underlyingActor.assignedEntities(zoneIdentifier).contains(entityChannel) shouldBe false
    }

    "accept to assign an entity in pending to an existing zone" in {
      val entityProbe = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier)
      val entityChannel = informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier)
      informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier)
      zoneManager.underlyingActor.pending.contains(zoneIdentifier) shouldBe true
      zoneManager.underlyingActor.pending(zoneIdentifier).contains(entityChannel) shouldBe true
      zoneManager.underlyingActor.assignedEntities(zoneIdentifier).contains(entityChannel) shouldBe false
    }

    "refuse to assign an entity already assigned" in {
      val entityProbe = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier, zoneIdentifier2)
      val entityChannel = informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier)
      zoneManager.tell(Ack, entityProbe.ref)
      zoneManager ! AssignEntityToZone(zoneIdentifier, entityChannel)
      expectMsgType[AlreadyAssigned]
      zoneManager ! AssignEntityToZone(zoneIdentifier2, entityChannel)
      expectMsgType[AlreadyAssigned]
    }
    "not have the same entity pending for two different zones" in {
      val entityProbe = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier, zoneIdentifier2)
      val entityChannel = informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier)
      zoneManager.underlyingActor.pending(zoneIdentifier).contains(entityChannel) shouldBe true
      zoneManager.underlyingActor.pending.contains(zoneIdentifier2) shouldBe false
      val entityChannel2 = informAndExpectMsgOnAssign(zoneManager, entityProbe, zoneIdentifier2)
      zoneManager.underlyingActor.pending(zoneIdentifier2).contains(entityChannel2) shouldBe true
      zoneManager.underlyingActor.pending.contains(zoneIdentifier) shouldBe false
    }
    /* --- END TEST ON ASSIGN --- */

    /* --- START TEST ON DISSOCIATE --- */
    "not dissociate an entity not associated" in {
      val entityProbe = TestProbe()
      zoneManager ! DissociateEntityFromZone(entityChannelWithRef(entityProbe.ref))
      expectMsg(AlreadyDissociated)
    }

    "dissociate an associated entity which was in pending and inform it" in {
      val entityProbeToDissociate = TestProbe()
      val entityProbeNotToDissociate = TestProbe()
      createZonesAndExpectMsg(zoneIdentifier)
      val entityChannel = informAndExpectMsgOnAssign(zoneManager, entityProbeToDissociate, zoneIdentifier)
      val entityChannelNotToDissociate = informAndExpectMsgOnAssign(zoneManager, entityProbeNotToDissociate, zoneIdentifier)
      zoneManager.underlyingActor.pending(zoneIdentifier).contains(entityChannel) shouldBe true
      zoneManager ! DissociateEntityFromZone(entityChannel)
      expectMsg(DissociateOk)
      entityProbeToDissociate.expectMsgType[DissociateFrom]
      zoneManager.underlyingActor.pending(zoneIdentifier).contains(entityChannelNotToDissociate) shouldBe true
      zoneManager.underlyingActor.pending(zoneIdentifier).contains(entityChannel) shouldBe false
    }

    "dissociate an associated entity which was in associatedEntitities, inform it and its zone" in {
      val entityProbe = TestProbe()
      val zoneProbe = TestProbe()
      val manager = TestActorRef(new ZoneManagerActor {
        override def createZoneActor(zoneID: String): ActorRef = zoneProbe.ref
      })
      manager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      val entityChannel = informAndExpectMsgOnAssign(manager, entityProbe, zoneIdentifier)
      manager.tell(Ack, entityProbe.ref)
      zoneProbe.expectMsgType[AddEntity]
      manager.underlyingActor.assignedEntities(zoneIdentifier).contains(entityChannel) shouldBe true
      manager ! DissociateEntityFromZone(entityChannel)
      expectMsg(DissociateOk)
      entityProbe.expectMsgType[DissociateFrom]
      zoneProbe.expectMsgType[DeleteEntity]
      manager.underlyingActor.assignedEntities(zoneIdentifier).contains(entityChannel) shouldBe false
    }
    /* --- END TEST ON DISSOCIATE --- */

    /* --- START TEST ON ACK --- */
    "move a pending entity to associatedEntities when Ack is received" in {
      val entityProbe = TestProbe()
      val zoneProbe = TestProbe()
      val manager = TestActorRef(new ZoneManagerActor {
        override def createZoneActor(zoneID: String): ActorRef = zoneProbe.ref
      })
      manager ! CreateZone(zoneIdentifier)
      expectMsg(ZoneCreated)
      manager.underlyingActor.zones(zoneIdentifier) shouldBe zoneProbe.ref
      val entityChannel = informAndExpectMsgOnAssign(manager, entityProbe, zoneIdentifier)
      manager.underlyingActor.pending(zoneIdentifier).contains(entityChannel) shouldBe true
      manager.underlyingActor.assignedEntities(zoneIdentifier).contains(entityChannel) shouldBe false
      manager.tell(Ack, entityProbe.ref)
      zoneProbe.expectMsgType[AddEntity]
      manager.underlyingActor.pending.contains(zoneIdentifier) shouldBe false
      manager.underlyingActor.assignedEntities(zoneIdentifier).contains(entityChannel) shouldBe true
    }
    /* --- END TEST ON ACK --- */
  }

  /* --- UTILITY METHODS --- */
  private def createZonesAndExpectMsg(identifiers: String*): List[ActorRef] = {
    identifiers.foreach {
      zoneID =>
        zoneManager ! CreateZone(zoneID)
        expectMsg(ZoneCreated)
    }
    zoneManager.underlyingActor.zones.values.toList
  }
  private def deleteZonesAndExpectMsg(identifiers: String*): Unit = {
    identifiers.foreach {
      zoneID =>
        zoneManager ! RemoveZone(zoneID)
        expectMsg(ZoneRemoved)
    }
  }
  private def entityChannelWithRef(entityRef: ActorRef): EntityChannel = {
    EntityChannel(RegisteredSensor("sensor", SensingCapability(Temperature)), entityRef)
  }

  private def informAndExpectMsgOnAssign(zoneManager: TestActorRef[ZoneManagerActor], entityProbe: TestProbe, zone: String): EntityChannel = {
    val entityChannel =  entityChannelWithRef(entityProbe.ref)
    zoneManager ! AssignEntityToZone(zone, entityChannel)
    entityProbe.expectMsgType[AssociateTo]
    expectMsg(AssignOk)
    entityChannel
  }

  case object Temperature extends Category
}

