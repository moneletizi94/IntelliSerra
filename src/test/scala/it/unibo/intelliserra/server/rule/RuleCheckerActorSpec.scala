package it.unibo.intelliserra.server.rule

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import it.unibo.intelliserra.common.communication.Messages.{DoActions, GetState, InferActions, MyState}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.zone.RuleCheckerActor
import it.unibo.intelliserra.server.zone.RuleCheckerActor.EvaluateActions
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Actions._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class RuleCheckerActorSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  var mockRuleCheckerRef : ActorRef = _
  val test = TestProbe("RuleEngineService")

  before{
    mockRuleCheckerRef = childActorOf(Props(RuleCheckerActor(2 seconds, test.path.toString)))
  }

  "A ruleCheckerActor " should {
    " ask state to his parent (zone) when receive CheckAction" in {
      mockRuleCheckerRef ! EvaluateActions
      expectMsg(GetState)
    }
  }

  "A ruleCheckerActor " should {
    " ask to infer actions to Rule Engine Service after receiving State from zone" in {
      mockRuleCheckerRef ! MyState(State.empty)
      test.expectMsg(InferActions(State.empty))
    }
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
