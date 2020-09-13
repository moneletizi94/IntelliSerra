package it.unibo.intelliserra.client.core

import it.unibo.intelliserra.core.rule.RuleInfo

import scala.concurrent.Future

trait RuleClient {
  /**
   * Get all rules.
   * @return all rules with info
   */
  def getRules: Future[List[RuleInfo]]

  /**
   * Enable an existing rule
   * @param ruleID, rule identifier
   * @return string who represents a Controller response.
   */
  def enableRule(ruleID: String): Future[String]

  /**
   * Disable an existing rule
   * @param ruleID, rule identifier
   * @return string who represents a Controller response.
   */
  def disableRule(ruleID: String): Future[String]
}
