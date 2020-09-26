package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.core.action.{Action, DoingActions, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.perception.Measure
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.RepeatedAction
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.entityManager.DeviceChannel
import it.unibo.intelliserra.server.zone.ZoneActor.ComputeMeasuresAggregation
import it.unibo.intelliserra.common.utils.Utils._
import it.unibo.intelliserra.server.rule.RuleEngineService

import scala.concurrent.duration.{FiniteDuration, _}

private[zone] class ZoneActor(private val aggregators: List[Aggregator],
                              override val repeatedActionRate : FiniteDuration,
                              val computeActionsRate : FiniteDuration)
                              extends Actor
                              with RepeatedAction[ComputeMeasuresAggregation]
                              with ActorLogging
                              with MessageReceivingLog {

  private[zone] var state : State = State.empty
  private[zone] var sensorsValue: Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities: Set[DeviceChannel] = Set()
  private[zone] var actuatorsState: Map[ActorRef, OperationalState] = Map()
  override val repeatedMessage: ComputeMeasuresAggregation = ComputeMeasuresAggregation()

  spawnRuleCheckerActor()

  override def receive: Receive = {
    case AddEntity(entityChannel) => associatedEntities += entityChannel
    case DeleteEntity(entityChannel) => associatedEntities -= entityChannel
    case GetState => sender ! MyState(state)
    case DoActions(actions) =>
      associatedEntities.map(deviceChannel => (deviceChannel.channel, capableOf(deviceChannel, actions))).filter(_._2.nonEmpty)
                        .foreach { case (actuatorRef, actionsToDo) => actuatorRef ! DoActions(actionsToDo) }
    case SensorMeasureUpdated(measure) => sensorsValue += sender -> measure
    case ComputeMeasuresAggregation() => state = computeState(); sensorsValue = Map()
    case ActuatorStateChanged(operationalState) =>
      actuatorsState += sender -> operationalState
      state = State(state.perceptions, computeActuatorsState());
  }

  private[zone] def capableOf(deviceChannel: DeviceChannel, actionToDo : Set[Action]) : Set[Action] = {
    actionToDo.filter(actionToDo => deviceChannel.device.capability.includes(Capability.acting(actionToDo.getClass)))
  }

  private[zone] def computeAggregatedPerceptions() : List[Measure] = {
    val measuresTry = for {
      (category, measures) <- sensorsValue.values.groupBy(_.category)
      aggregator <- aggregators.find(_.category == category)
    } yield aggregator.aggregate(measures.toList)
    flattenTryIterable(measuresTry)(e => log.error(e,"incompatible measures type"))(identity).toList
  }

  private[zone] def computeActuatorsState() : List[Action] = actuatorsState.values.flatMap({
    case DoingActions(actions) => actions
    case Idle => Nil
  }).toList.distinct

  private[zone] def computeState() : State = {
    State(computeAggregatedPerceptions(), computeActuatorsState())
  }

  private def spawnRuleCheckerActor() : ActorRef = {
    context.actorOf(Props(RuleCheckerActor(computeActionsRate, s"../../${RuleEngineService.name}")))
  }

}

object ZoneActor {
  case class ComputeMeasuresAggregation()
  private val defaultStateEvaluationRate = 10 seconds
  private val defaultActionsEvaluationRate = 10 seconds

  def apply(name: String,
            aggregators: List[Aggregator],
            computeStateRate : FiniteDuration = defaultStateEvaluationRate,
            computeActionsRate : FiniteDuration = defaultActionsEvaluationRate)
           (implicit system: ActorSystem): ActorRef = {
    system actorOf (props(aggregators, computeStateRate, computeActionsRate), name)
  }

  def props(aggregators: List[Aggregator],
            computeStateRate : FiniteDuration = defaultStateEvaluationRate,
            computeActionsRate : FiniteDuration = defaultActionsEvaluationRate): Props = {
    require(aggregators hasUniqueValueForProperty (_.category), "only one aggregator must be assigned for each category")
    Props(new ZoneActor(aggregators, computeStateRate, computeActionsRate))
  }

}
