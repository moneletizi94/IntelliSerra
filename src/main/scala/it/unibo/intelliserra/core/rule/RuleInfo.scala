package it.unibo.intelliserra.core.rule

/**
 * RuleInfo represents the information of a rule.
 * Contains a defined rule and its identifier
 *
 * @param identifier rule identifier
 * @param rule represents a specific rule
 */
final case class RuleInfo(identifier: String, rule: Rule)
