package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom, DoActions}
import it.unibo.intelliserra.core.actuator.{Actuator, Idle, OperationalState, TimedTask}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.device.core.actuator.ActuatorActor.{ActuatorStateChanged, OnCompleteAction}
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, OpenWindow, Water}
import org.junit.runner.RunWith
import org.mockito.scalatest.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration._
import scala.reflect.ClassTag

@RunWith(classOf[JUnitRunner])
class ActuatorActorSpec extends TestKit(ActorSystem("device"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with MockitoSugar
  with TestUtility {

  private val ActuatorName = "MockActuator"
  private val ActuatorCapability = Capability.acting(Water, OpenWindow)
  private val NotSupportedAction = Fan
  private var actuator: Actuator = _
  private var actuatorActor: TestActorRef[ActuatorActor] = _

  before {
    actuator = spy(mockActuator(ActuatorName, ActuatorCapability) {
      case (_, _) => TimedTask(1 seconds)
    })
    actuatorActor = TestActorRef.create(system, ActuatorActor.props(actuator))
  }

  after {
    actuatorActor ! PoisonPill
  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  "An actuator" must {

    "handle init event when actor start" in {
      verify(actuator).onInit()
    }

    "handle association event when is associated to zone from server" in {
      associateToZone(actuatorActor)
      verify(actuator).onAssociateZone(any[String])
    }

    "handle dissociation event when is dissociated to zone from server" in {
      associateToZone(actuatorActor)
      dissociateToZone(actuatorActor)
      verify(actuator).onDissociateZone(any[String])
    }

    "ignore actions if is not associated to zone" in {
      actuatorActor ! DoActions(ActuatorCapability.actions)
      expectNoMessage()
    }

    "send its operational state if an action is completed" in {
      associateToZone(actuatorActor)
      actuatorActor ! OnCompleteAction(Water, TimedTask.now())
      expectMsgType[ActuatorStateChanged]
    }

    "dispatch action and return to Idle when complete" in {
      val todoActions = ActuatorCapability.actions
      associateToZone(actuatorActor)
      actuatorActor ! DoActions(todoActions)
      expectMsg(ActuatorStateChanged(OperationalState(todoActions)))
      expectIdleState(todoActions.size)
    }

    "complete only the actions declared as its capability" in {
      val todoActions = ActuatorCapability.actions + NotSupportedAction
      associateToZone(actuatorActor)
      actuatorActor ! DoActions(todoActions)
      expectMsg(ActuatorStateChanged(OperationalState(ActuatorCapability.actions)))
      expectIdleState(ActuatorCapability.actions.size)
    }
  }

  private def associateToZone(actor: ActorRef): Unit = {
    actor.tell(AssociateTo(testActor, testActorName), testActor)
    expectMsg(Ack)
  }

  private def dissociateToZone(actor: ActorRef): Unit = {
    actor.tell(DissociateFrom(testActor, testActorName), testActor)
  }

  private def expectIdleState(pendingActions: Int): Unit = {
    collectMsgType[ActuatorStateChanged](pendingActions).last.operationalState shouldBe Idle
  }

  private def collectMsgType[T](collectSize: Int)(implicit t: ClassTag[T]): List[T] = {
    1 to collectSize map { _ => expectMsgType[T] } toList
  }
}
