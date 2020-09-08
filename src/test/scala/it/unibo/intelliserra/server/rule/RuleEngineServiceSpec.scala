package it.unibo.intelliserra.server.rule

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import it.unibo.intelliserra.common.communication.Messages.{DisableRule, InferActions, EnableRule, GetRules, NotFound, Ok, Rules}
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.{Rule, RuleInfo, StatementTestUtils}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Actions.{OpenWindow, Water}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RuleEngineServiceSpec extends TestKit(ActorSystem("RuleEngineServiceSpec"))
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll
  with StatementTestUtils {

  private var ruleEngineService: TestActorRef[RuleEngineService] = _
  private var rule: Rule = _
  private var state: State = _
  private val actionSet: Set[Action] = Set(Water, OpenWindow)
  private val ruleID = "rule0"
  private val rule1ID = "rule1"

  before {
    rule = Rule(temperatureStatement, actionSet)
    state = State(List(), List())
    ruleEngineService = TestActorRef.create(system, Props(new RuleEngineService(List(rule))))
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A ruleEngineService" should {
    "obtain all rules" in{
      ruleEngineService ! GetRules
      expectMsg(Rules(List(RuleInfo(ruleID, rule))))
    }

    "enable an existing rule" in {
      ruleEngineService ! EnableRule(ruleID)
      expectMsg(Ok)
    }

    "not enable an nonexistent rule" in {
      ruleEngineService ! EnableRule(rule1ID)
      expectMsg(NotFound)
    }

    "disable an existing rule" in {
      ruleEngineService ! DisableRule(ruleID)
      expectMsg(Ok)
    }

    "not disable an nonexistent rule" in {
      ruleEngineService ! DisableRule(rule1ID)
      expectMsg(NotFound)
    }

    "deduce a set of actions starting from the state" in {
      ruleEngineService ! InferActions(state)
    }
  }
}