package it.unibo.intelliserra.core.rule

import alice.tuprolog.{Prolog, Struct, Theory}
import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.prolog.Representations._
import it.unibo.intelliserra.core.prolog.RichAny
import it.unibo.intelliserra.core.prolog.RichProlog
import it.unibo.intelliserra.core.state.State

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * This trait represent a RuleEngine interface to interact with Rule.
 * RuleEngine contains all the rules with the ability to enable and disable them.
 */
trait RuleEngine {

  /**
   * Returns a set of possible actions that can be performed.
   * This method asks the prolog engine to resolve a new Theory.
   *
   * @param state represents the state of zone
   * @return set of actions
   */
  def inferActions(state: State): Set[Action]

  /**
   * Method to enabled an existing rule
   *
   * @param ruleID rule identifier
   * @return boolean, if rule exist true otherwise false
   */
  def enableRule(ruleID: String): Boolean

  /**
   * Method to disabled an existing rule
   *
   * @param ruleID rule identifier
   * @return boolean if rule exist true otherwise false
   */
  def disableRule(ruleID: String): Boolean

  def rules: List[RuleInfo]
}

object RuleEngine {

  def apply(rules: List[Rule]): RuleEngine = PrologRuleEngine(rules.zipWithIndex.map(pair => RuleInfo(s"rule${pair._2}", pair._1)))

  def apply(rules: Map[String, Rule]): RuleEngine = PrologRuleEngine(rules.map(pair => RuleInfo(pair._1, pair._2)).toList)

  private[rule] case class PrologRuleEngine(override val rules: List[RuleInfo]) extends RuleEngine {

    private val engine = initializeProlog

    private[rule] var rulesMode: Map[String, Boolean] = rules.map(rule => (rule.identifier, true)).toMap

    override def inferActions(state: State): Set[Action] =
      Try {
        engine.solve(s"infer(${state.toTerm},ACTIONS)")
          .getTerm("ACTIONS")
      }
        .fold(_ => List(), listTerm => listTerm.castTo(classOf[Struct]).listIterator().asScala.toList)
        .map { actionSolve => rules.flatMap(_.rule.actions).find(action => action.toTerm == actionSolve) }
        .filter(_.isDefined)
        .map(_.get).toSet

    override def enableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, !_).fold(false)(ruleClause => engine.assertTermClauses(ruleClause.toTerm.castTo(classOf[Struct])))
    }

    override def disableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, identity).fold(false)(ruleClause => engine.retractTermClauses(ruleClause.toTerm.castTo(classOf[Struct])))
    }

    private def ruleChecker(ruleID: String, p: Boolean => Boolean): Option[Rule] = {
      val optionRule = rulesMode.get(ruleID).filter(p)
      rulesMode = optionRule.fold(rulesMode)(ruleValue => rulesMode + (ruleID -> !ruleValue))
      optionRule.fold[Option[Rule]](None)(_ => rules.find(_.identifier == ruleID).map(_.rule))
    }

    /**
     * This method initializes the prolog engine.
     * Loads the whole theory from file and set of all rules.
     */
    private def initializeProlog: Prolog = {
      val engine = new Prolog()
      val file = scala.io.Source.fromResource("greenhouse-theory.pl")
      val lines = file.mkString
      file.close()
      engine.setTheory(new Theory(lines))
      rules.map { ruleInfo => ruleInfo.rule.toTerm }
        .filter(_.isList)
        .map{rule => engine.assertTermClauses(rule.castTo(classOf[Struct]))}
      engine
    }
  }

}