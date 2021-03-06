package it.unibo.intelliserra.server.rule

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages.{DisableOk, DisableRule, DoActions, EnableOk, EnableRule, Error}
import it.unibo.intelliserra.common.communication.Messages.{GetRules, InferActions, RuleEntityResponse, Rules}
import it.unibo.intelliserra.core.rule.{Rule, RuleEngine}

/**
 * This class represents a RuleEngineService.
 * It receives messages to enable or disable the rules and deduces the actions to be done from the state.
 *
 * @param rules represents a list of Rule.
 */
private[server] class RuleEngineService(private val rules: List[Rule]) extends Actor with ActorLogging {

  private val ruleEngine = RuleEngine(rules)

  /**
   * Receive function contains all messages that can be received by the RuleEngineService.
   *
   * @return the response at the sender.
   */
  override def receive: Receive = {

    case EnableRule(ruleID) => sendResponse(ruleEngine.enableRule(ruleID), EnableOk)

    case DisableRule(ruleID) => sendResponse(ruleEngine.disableRule(ruleID), DisableOk)

    case InferActions(state) => sender ! DoActions(ruleEngine.inferActions(state))

    case GetRules => sender ! Rules(ruleEngine.rules)
  }

  /**
   * This function will be used to create the reply to be sent to the sender.
   *
   * @param ruleChecked boolean who represents if rule exists or not.
   */
  private def sendResponse(ruleChecked: Boolean, response: RuleEntityResponse): Unit = {
    if (ruleChecked) sender ! response else sender ! Error
  }
}

/** Factory for [[it.unibo.intelliserra.server.rule.RuleEngineService]] instances. */
object RuleEngineService {
  val name = "RuleEngineService"

  /** Creates a RuleEngineService actor with a given all rules.
   *
   * @param rules list of rules, it contains all rules
   * @param actorSystem represent the actorSystem
   * @return an actorRef representing an actor, which is a new RuleEngineService with all rules and the specified name.
   */
  def apply(rules: List[Rule])(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf(Props(new RuleEngineService(rules)), name)

}
