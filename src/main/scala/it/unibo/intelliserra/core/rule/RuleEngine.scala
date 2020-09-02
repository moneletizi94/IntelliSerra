package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.state.State

trait RuleEngine {
  def inferActions(state: State): Set[Action]

  def enabledRule(ruleID: String): Boolean

  def disableRule(ruleID: String): Boolean

  def rules: List[RuleInfo]
}

object RuleEngine {

  def apply(rules: List[Rule]): RuleEngine = RuleEngineImpl(rules.zipWithIndex.map(pair => RuleInfo(s"rule${pair._2}", pair._1)))
  def apply(rules : Map[String, Rule]): RuleEngine = RuleEngineImpl(rules.map(pair => RuleInfo(pair._1, pair._2)).toList)

  private[rule] case class RuleEngineImpl(override val rules: List[RuleInfo]) extends RuleEngine {

    private[rule] var rulesMode: Map[String, Boolean] = rules.map(rule => (rule.identifier, false)).toMap

    override def inferActions(state: State): Set[Action] = ???

    override def enabledRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, !_)
    }

    override def disableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, _ => true)
    }

    private def ruleChecker(ruleID: String, f: Boolean => Boolean): Boolean = {
      if(rulesMode.contains(ruleID)) { rulesMode += (ruleID -> rulesMode.get(ruleID).filter(f(_)).map(!_).get) ; true } else false
    }
  }

}