package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorPath}
import it.unibo.intelliserra.common.communication.Messages.{DoActions, GetState, InferActions, MyState}
import it.unibo.intelliserra.server.RepeatedAction
import it.unibo.intelliserra.server.zone.RuleCheckerActor.EvaluateActions

import scala.concurrent.duration.FiniteDuration

class RuleCheckerActor(override val rate: FiniteDuration, ruleEnginePath : String) extends Actor
                                                                                    with RepeatedAction[EvaluateActions]
                                                                                    with ActorLogging{

  val ruleEngineService = context.actorSelection(ruleEnginePath)

  override def receive: Receive = {
    case EvaluateActions() => context.parent ! GetState ; log.info("get zone’s state to evaluate action to do based on rules")
    case MyState(state) => ruleEngineService.tell(InferActions(state), context.parent)
  }

  override val repeatedMessage: EvaluateActions = EvaluateActions()
}

object RuleCheckerActor{
  case class EvaluateActions()
  def apply(rate: FiniteDuration, ruleEnginePath : String = "../RuleEngineService"): RuleCheckerActor = new RuleCheckerActor(rate,ruleEnginePath)
}

