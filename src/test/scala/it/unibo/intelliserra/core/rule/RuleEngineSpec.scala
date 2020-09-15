package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.MajorOperator
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, OpenWindow, Water}
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RuleEngineSpec extends WordSpecLike with Matchers with BeforeAndAfter with StatementTestUtils {

  private var ruleEngine: RuleEngine = _
  private var ruleEngineEmpty: RuleEngine = _
  private var rule: Rule = _
  private var rule2 : Rule = _
  private val actionSet: Set[Action] = Set(Water, OpenWindow)
  private val actionSet2: Set[Action] = Set(Water, Fan)

  private val rule1ID = "rule1"
  private val rule2ID = "rule2"
  private val measure = Measure(Temperature)(temperatureValue + 1)
  private val measure2 = Measure(Humidity)(humidityValue + 1)
  private val state = State(List(measure, measure2), List())

  before{
    rule = Rule(temperatureStatement, actionSet)
    rule2 = Rule(humidityStatement, actionSet2)
    ruleEngine = RuleEngine(Map(
      rule1ID -> rule,
      rule2ID -> rule2,
    ))
  }

  "A ruleEngine" should {

    "contain list of rules" in {
      ruleEngine.rules.isEmpty shouldBe false
    }

    "contain an empty list if it has no rules yet" in {
      ruleEngineEmpty = RuleEngine(List())
      ruleEngineEmpty.rules.isEmpty shouldBe true
    }

    "enable a rule that is not enabled" in {
      ruleEngine.disableRule(rule1ID) shouldBe true
      ruleEngine.enableRule(rule1ID) shouldBe true
    }

    "enable a rule that is already enabled" in {
      ruleEngine.disableRule(rule1ID) shouldBe true
      ruleEngine.enableRule(rule1ID) shouldBe true
      ruleEngine.enableRule(rule1ID) shouldBe false
    }

    "disabled a rule that is not disabled" in {
      ruleEngine.disableRule(rule1ID) shouldBe true
    }

    "disabled a rule that is already disabled" in {
      ruleEngine.disableRule(rule1ID) shouldBe true
      ruleEngine.disableRule(rule1ID) shouldBe false
    }

    "infer action from an existing state" in {
      ruleEngine.inferActions(state) shouldBe actionSet ++ actionSet2
    }

    "not infer action of disabled rule" in {
      ruleEngine.disableRule(rule1ID)
      ruleEngine.inferActions(state) shouldBe actionSet2
      ruleEngine.disableRule(rule2ID)
      ruleEngine.inferActions(state) shouldBe Set()
    }

    "must infer action from re-enabled rule" in {
      ruleEngine.disableRule(rule1ID)
      ruleEngine.inferActions(state) shouldBe actionSet2
      ruleEngine.enableRule(rule1ID)
      ruleEngine.inferActions(state) shouldBe actionSet ++ actionSet2
    }

  }
}
