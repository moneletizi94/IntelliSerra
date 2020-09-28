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
import it.unibo.intelliserra.server.ServerConfig.ZoneConfig
import it.unibo.intelliserra.server.rule.RuleEngineService

import scala.concurrent.duration.{FiniteDuration, _}

private[zone] class ZoneActor(private val aggregators: List[Aggregator],
                              override val repeatedActionRate : FiniteDuration,
                              val computeActionsRate : FiniteDuration)
                              extends Actor
                              with RepeatedAction[ComputeMeasuresAggregation]
                              with ActorLogging {

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
    case DoActions(actions) => sendToCorrectActuators(actions)
    case SensorMeasureUpdated(measure) => sensorsValue += sender -> measure
    case ComputeMeasuresAggregation() => state = computeState(); sensorsValue = Map()
    case ActuatorStateChanged(operationalState) =>
      actuatorsState += sender -> operationalState
      state = State(state.perceptions, computeActuatorsState());
  }

  private[zone] def computeAggregatedPerceptions() : List[Measure] = {
    val measuresTry = for {
      (category, measures) <- sensorsValue.values.groupBy(_.category)
      aggregator <- aggregators.find(_.category == category)
    } yield aggregator.aggregate(measures.toList)
    flattenTryIterable(measuresTry)(e => log.error(e,"incompatible measures type"))(identity).toList
  }

  private[zone] def getDeviceAndCapableActions(actions : Set[Action]) : Map[DeviceChannel, Traversable[Action]] = {
    (for {
      associatedEntity <- associatedEntities
      action <- actions
      capabilitiesByActionToDo = Capability.acting(action.getClass)
      if associatedEntity.device.capability.includes(capabilitiesByActionToDo)
    } yield (associatedEntity, action)).toMultiMap
  }

  private[zone] def sendToCorrectActuators(actions: Set[Action]): Unit = {
    getDeviceAndCapableActions(actions).foreach({
      case (deviceChannel,capableActions) => deviceChannel.channel ! DoActions(capableActions.toSet)
    })
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

  implicit class RichPairTraversable[A,B](traversable: Traversable[(A,B)]){
    def toMultiMap: Map[A, Traversable[B]] = traversable groupBy (_._1) mapValues (_ map (_._2))
  }
}

object ZoneActor {
  case class ComputeMeasuresAggregation()
  private val defaultStateEvaluationRate = 10 seconds
  private val defaultActionsEvaluationRate = 10 seconds

  def apply(name: String,
            config: ZoneConfig)
           (implicit system: ActorSystem): ActorRef = {
    system actorOf (props(config.aggregators, config.stateEvaluationPeriod, config.actionsEvaluationPeriod), name)
  }

  def props(aggregators: List[Aggregator],
            computeStateRate : FiniteDuration = defaultStateEvaluationRate,
            computeActionsRate : FiniteDuration = defaultActionsEvaluationRate): Props = {
    require(aggregators hasUniqueValueForProperty (_.category), "only one aggregator must be assigned for each category")
    Props(new ZoneActor(aggregators, computeStateRate, computeActionsRate))
  }

}
