package it.unibo.intelliserra.server.zone

import akka.actor.Actor
import it.unibo.intelliserra.common.communication.Messages.{DoActions, GetState, MyState}
import it.unibo.intelliserra.server.ActorWithRepeatedAction
import it.unibo.intelliserra.server.zone.RuleCheckerActor.CheckActions

import scala.concurrent.duration.FiniteDuration

class RuleCheckerActor(override val rate: FiniteDuration) extends Actor with ActorWithRepeatedAction[CheckActions]{

  val ruleEngineService = context.actorSelection("/RuleEngineService")

  override def receive: Receive = {
    case CheckActions => context.parent ! GetState
    case MyState(stateOpt) => //stateOpt.foreach(ruleEngineService ! InferActions(_))
    case DoActions(actions) => context.parent ! _
  }

  override val repeatedMessage: CheckActions = CheckActions()
}

object RuleCheckerActor{
  case class CheckActions()

  def apply(rate: FiniteDuration): RuleCheckerActor = new RuleCheckerActor(rate)
}

