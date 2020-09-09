package it.unibo.intelliserra.device.core.actuator

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{Ack, AssociateTo, DissociateFrom, DoActions}
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.actuator.{Actuator, Idle, OperationalState, TimedTask}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.ActingCapability
import it.unibo.intelliserra.device.core.actuator.ActuatorActor.{ActuatorStateChanged, OnCompleteAction}
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, OpenWindow, Water}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Future
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


  private def mockActuator(name: String,
                           actingCapability: ActingCapability)
                          (actionHandlerCallback: ActionHandler = { case p@_ => TimedTask.now(p._2)},
                           onInitCallback: => Unit = { },
                           onAssociateZoneCallback: String => Unit = { _ => },
                           onDissociateZoneCallback: String => Unit = { _ => }): Actuator = new Actuator {

    override def identifier: String = name
    override def capability: Capability.ActingCapability = actingCapability
    override def actionHandler: ActionHandler = actionHandlerCallback
    override def onInit(): Unit = onInitCallback
    override def onAssociateZone(zoneName: String): Unit = onAssociateZoneCallback(zoneName)
    override def onDissociateZone(zoneName: String): Unit = onDissociateZoneCallback(zoneName)
  }

  private val ActuatorName = "MockActuator"
  private val ActuatorCapability = Capability.acting(Water, OpenWindow)
  private val NotSupportedAction = Fan
  private var actuator: Actuator = _
  private var actuatorActor: TestActorRef[ActuatorActor] = _

  before {
    actuator = mockActuator(ActuatorName, ActuatorCapability) {
      case (_, action) => TimedTask(action, 1 seconds)(_ => ())
    }
    actuatorActor = TestActorRef.create(system, ActuatorActor.props(actuator))
    actuatorActor.tell(AssociateTo(testActor, testActorName), testActor)
    expectMsg(Ack)
  }

  after {
    actuatorActor ! PoisonPill
  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  "An actuator" must {

    "ignore actions if is not associated to zone" in {
      actuatorActor ! DissociateFrom(testActor, testActorName)
      actuatorActor ! DoActions(ActuatorCapability.actions)
      expectNoMessage()
    }

    "send its operational state if an action is completed" in {
      actuatorActor ! OnCompleteAction(TimedTask.now(Water))
      expectMsgType[ActuatorStateChanged]
    }

    "dispatch action and return to Idle when complete" in {
      val todoActions = ActuatorCapability.actions
      actuatorActor ! DoActions(todoActions)
      expectMsg(ActuatorStateChanged(OperationalState(todoActions)))
      expectIdleState(todoActions.size)
    }

    "complete only the actions declared as its capability" in {
      val todoActions = ActuatorCapability.actions + NotSupportedAction
      actuatorActor ! DoActions(todoActions)
      expectMsg(ActuatorStateChanged(OperationalState(ActuatorCapability.actions)))
      expectIdleState(ActuatorCapability.actions.size)
    }
  }

  private def expectIdleState(pendingActions: Int): Unit = {
    collectMsgType[ActuatorStateChanged](pendingActions).last.operationalState shouldBe Idle
  }

  private def collectMsgType[T](collectSize: Int)(implicit t: ClassTag[T]): List[T] = {
    1 to collectSize map { _ => expectMsgType[T] } toList
  }
}
