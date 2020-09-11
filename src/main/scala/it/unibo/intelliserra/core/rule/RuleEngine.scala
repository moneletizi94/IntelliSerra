package it.unibo.intelliserra.core.rule

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.prolog.Representations._
import it.unibo.intelliserra.core.prolog.RichAny
import it.unibo.intelliserra.core.state.State

/**
 * This trait represent a RuleEngine interface to interact with Rule.
 * RuleEngine contains all the rules with the ability to enable and disable them.
 */
trait RuleEngine {
  def inferActions(state: State): Set[Action]

  def enableRule(ruleID: String): Boolean

  def disableRule(ruleID: String): Boolean

  def rules: List[RuleInfo]
}

object RuleEngine {

  def apply(rules: List[Rule]): RuleEngine = RuleEngineImpl(rules.zipWithIndex.map(pair => RuleInfo(s"rule${pair._2}", pair._1)))

  def apply(rules: Map[String, Rule]): RuleEngine = RuleEngineImpl(rules.map(pair => RuleInfo(pair._1, pair._2)).toList)

  private[rule] case class RuleEngineImpl(override val rules: List[RuleInfo]) extends RuleEngine {

    private[rule] var rulesMode: Map[String, Boolean] = rules.map(rule => (rule.identifier, false)).toMap

    /**
     * Returns a set of possible actions that can be performed
     *
     * @param state represents the state of zone
     * @return set of actions
     */
    override def inferActions(state: State): Set[Action] = ???

    /**
     * Method to enabled an existing rule
     *
     * @param ruleID rule identifier
     * @return boolean, if rule exist true otherwise false
     */
    override def enableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, !_)
    }

    /**
     * Method to disabled an existing rule
     *
     * @param ruleID rule identifier
     * @return boolean if rule exist true otherwise false
     */
    override def disableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, _ => true)
    }

    /**
     * This method check if rule exist and change her boolean condition who represent rule's mode.
     *
     * @param ruleID rule identifier
     * @param f function to filter
     * @return if rule exist true otherwise false
     */
    private def ruleChecker(ruleID: String, f: Boolean => Boolean): Boolean = {
      if (rulesMode.contains(ruleID)) { rulesMode += (ruleID -> rulesMode.get(ruleID).filter(f(_)).map(!_).get); true } else false
    }
  }

}