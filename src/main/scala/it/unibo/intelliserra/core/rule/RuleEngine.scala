package it.unibo.intelliserra.core.rule

import alice.tuprolog.{Prolog, Struct, Theory}
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.prolog.Representations._
import it.unibo.intelliserra.core.prolog.RichAny
import it.unibo.intelliserra.core.state.State

import scala.collection.JavaConverters._
import scala.util.Try

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

  def apply(rules: List[Rule]): RuleEngine = PrologRuleEngine(rules.zipWithIndex.map(pair => RuleInfo(s"rule${pair._2}", pair._1)))

  def apply(rules: Map[String, Rule]): RuleEngine = PrologRuleEngine(rules.map(pair => RuleInfo(pair._1, pair._2)).toList)

  private[rule] case class PrologRuleEngine(override val rules: List[RuleInfo]) extends RuleEngine {

    private val engine = initializeProlog

    private[rule] var rulesMode: Map[String, Boolean] = rules.map(rule => (rule.identifier, true)).toMap

    /**
     * Returns a set of possible actions that can be performed.
     * This method asks the prolog engine to resolve a new Theory.
     *
     * @param state represents the state of zone
     * @return set of actions
     */
    override def inferActions(state: State): Set[Action] =
      Try {
        engine.solve(s"infer(${state.toTerm},ACTIONS)")
          .getTerm("ACTIONS")
      }
        .fold(_ => List(), listTerm => listTerm.castTo(classOf[Struct]).listIterator().asScala.toList)
        .map { actionSolve => rules.flatMap(_.rule.actions).find(action => action.toTerm == actionSolve) }
        .filter(_.isDefined)
        .map(_.get).toSet

    /**
     * Method to enabled an existing rule
     *
     * @param ruleID rule identifier
     * @return boolean, if rule exist true otherwise false
     */
    override def enableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, !_).fold(false)(ruleClause => prologRuleStateUpdate(ruleClause, "assert"))
        /*
        Theory.fromPrologList(ruleClause.toTerm.castTo(classOf[Struct])).getClauses.asScala
          .foreach(rule => engine.solve(s"assert($rule)"))
        true
      })*/
    }

    /**
     * Method to disabled an existing rule
     *
     * @param ruleID rule identifier
     * @return boolean if rule exist true otherwise false
     */
    override def disableRule(ruleID: String): Boolean = {
      ruleChecker(ruleID, identity).fold(false)(ruleClause => prologRuleStateUpdate(ruleClause, "retract"))
    }

    /**
     * This method check if rule exist and change her boolean condition's mode.
     *
     * @param ruleID rule identifier
     * @return if rule exist true otherwise false
     */
    private def ruleChecker(ruleID: String, p: Boolean => Boolean): Option[Rule] = {
      val optionRule = rulesMode.get(ruleID).filter(p)
      rulesMode = optionRule.fold(rulesMode)(ruleValue => rulesMode + (ruleID -> !ruleValue))
      optionRule.fold[Option[Rule]](None)(_ => rules.find(_.identifier == ruleID).map(_.rule))
    }

    /**
     * This method initializes the prolog engine.
     * Loads the whole theory from file and set of all rules.
     *
     * @return prolog engine
     */
    private def initializeProlog: Prolog = {
      val engine = new Prolog()
      val file = scala.io.Source.fromResource("greenhouse-theory.pl")
      val lines = file.mkString
      file.close()
      engine.setTheory(new Theory(lines))
      rules.map { ruleInfo => ruleInfo.rule.toTerm }
        .filter(_.isList)
        .flatMap { rule => Theory.fromPrologList(rule.castTo(classOf[Struct])).getClauses.asScala }
        .foreach(ruleClause => engine.solve(s"assert($ruleClause)"))

      engine
    }

    private def prologRuleStateUpdate(ruleClause: Rule, str: String): Boolean = {
      Theory.fromPrologList(ruleClause.toTerm.castTo(classOf[Struct])).getClauses.asScala
        .foreach(rule => engine.solve(s"$str($rule)"))
      true
    }
  }
}