package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{Ack, ActuatorStateChanged, AssociateTo, DissociateFrom, DoActions}
import it.unibo.intelliserra.core.action.{Action, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.device.core.actuator.ActuatorActor.OnOperationCompleted
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, OpenWindow, Water}
import org.junit.runner.RunWith
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
  with TestUtility {

  private val ActuatorName = "MockActuator"
  private val ActuatorActions: Set[Action] = Set(Water, OpenWindow)
  private val ActuatorCapability = Capability.acting(ActuatorActions.map(_.getClass))
  private val NotSupportedAction = Fan
  private var actuator: Actuator = _
  private var actuatorActor: TestActorRef[ActuatorActor] = _

  before {
    actuator = mockActuator(ActuatorName, ActuatorCapability) {
      case (_, _) => Operation.completeAfter(1 seconds)
    }
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

    "send an ack when is associated to zone from server" in {
      associateToZone(actuatorActor)
    }

    "ignore actions if is not associated to zone" in {
      associateToZone(actuatorActor)
      dissociateToZone(actuatorActor)
      actuatorActor ! DoActions(ActuatorActions.toSet)
      expectNoMessage()
    }

    "send its operational state if an action is completed" in {
      associateToZone(actuatorActor)
      actuatorActor ! OnOperationCompleted(Water)
      expectMsgType[ActuatorStateChanged]
    }

    "dispatch action and return to Idle when complete" in {
      val todoActions = ActuatorActions
      associateToZone(actuatorActor)
      actuatorActor ! DoActions(todoActions)
      expectMsg(ActuatorStateChanged(OperationalState(todoActions)))
      expectIdleState(todoActions.size)
    }

    "complete only the actions declared as its capability" in {
      val todoActions = ActuatorActions + NotSupportedAction
      associateToZone(actuatorActor)
      actuatorActor ! DoActions(todoActions)
      expectMsg(ActuatorStateChanged(OperationalState(ActuatorActions)))
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
