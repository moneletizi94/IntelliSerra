package it.unibo.intelliserra.server.rule

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages.{DisableRule, DoActions, InferActions, EnableRule}
import it.unibo.intelliserra.common.communication.Protocol.{NotFound, Ok, ServiceResponse, Error}
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

    case EnableRule(ruleID) => sendResponse(ruleEngine.enableRule(ruleID))

    case DisableRule(ruleID) => sendResponse(ruleEngine.disableRule(ruleID))

    case InferActions(state) => sender ! DoActions(ruleEngine.inferActions(state))
  }

  /**
   * This function will be used to create the reply to be sent to the sender.
   *
   * @param ruleChecked boolean who represents if rule exists or not.
   */
  private def sendResponse(ruleChecked: Boolean): Unit = {
    if (ruleChecked) sender ! ServiceResponse(Ok) else sender ! ServiceResponse(Error, "not possible")
  }
}

/**
 * Object for RuleEngineService.
 * It contains an apply function to create a instance of RuleEngineImpl.
 */
object RuleEngineService {
  val name = "RuleEngineService"

  def apply(rules: List[Rule])(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf(Props(new RuleEngineService(rules)), name)

}
