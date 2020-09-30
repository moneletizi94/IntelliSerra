package it.unibo.intelliserra.client.core

import it.unibo.intelliserra.core.rule.RuleInfo

import scala.concurrent.Future

trait RuleClient {

  /**
   * Get all rules.
   * @return if success, all rules with info, a failure otherwise
   */
  def getRules: Future[List[RuleInfo]]

  /**
   * Enable an existing rule
   * @param ruleID, rule identifier
   * @return if success, string who represents a Controller response, a failure otherwise
   */
  def enableRule(ruleID: String): Future[String]

  /**
   * Disable an existing rule
   * @param ruleID, rule identifier
   * @return if success, string who represents a Controller response, a failure otherwise
   */
  def disableRule(ruleID: String): Future[String]
}
