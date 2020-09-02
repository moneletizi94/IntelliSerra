package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.dsl.ConditionStatement.SimpleConditionStatement
import it.unibo.intelliserra.core.rule.dsl.{EqualsOperator, MajorOperator}
import it.unibo.intelliserra.core.sensor.Category
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RuleEngineSpec extends WordSpecLike with Matchers with BeforeAndAfter {

  private object Water extends Action

  private object OpenWindow extends Action

  private object Temperature extends Category

  private var ruleEngine: RuleEngine = _
  private var ruleEngineEmpty: RuleEngine = _
  private var rule: Rule = _
  private val actionSet: Set[Action] = Set(Water, OpenWindow)

  private val rule1ID = "rule1"
  private val temperatureValue = 20
  private val temperatureStatement = SimpleConditionStatement(Temperature, MajorOperator, temperatureValue)


  before{
    rule = Rule(temperatureStatement, actionSet)
    ruleEngine = RuleEngine(Map(
      rule1ID -> rule
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
      ruleEngine.enabledRule(rule1ID) shouldBe true
    }

    "disabled a rule that is not disabled" in {
      ruleEngine.enabledRule(rule1ID) shouldBe true
      ruleEngine.disableRule(rule1ID) shouldBe true
    }
  }
}
