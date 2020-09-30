package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorPath}
import it.unibo.intelliserra.common.communication.Messages.{DoActions, GetState, InferActions, MyState}
import it.unibo.intelliserra.server.RepeatedAction
import it.unibo.intelliserra.server.zone.RuleCheckerActor.EvaluateActions

import scala.concurrent.duration.FiniteDuration

class RuleCheckerActor(override val repeatedActionRate: FiniteDuration, ruleEnginePath : String) extends Actor
                                                                                    with RepeatedAction[EvaluateActions]
                                                                                    with ActorLogging{

  val ruleEngineService = context.actorSelection(ruleEnginePath)

  override def receive: Receive = {
    case EvaluateActions() => context.parent ! GetState
    case MyState(state) => ruleEngineService.tell(InferActions(state), context.parent)
  }

  override val repeatedMessage: EvaluateActions = EvaluateActions()
}

object RuleCheckerActor{
  case class EvaluateActions()

  /**
   *
   * @param rate periodical time for the evaluation of the rules
   * @param ruleEnginePath the path of the rule engine in order to contact it
   * @return  an actor ref of rule checker actor
   */
  def apply(rate: FiniteDuration, ruleEnginePath : String = "../RuleEngineService"): RuleCheckerActor = new RuleCheckerActor(rate,ruleEnginePath)
}

