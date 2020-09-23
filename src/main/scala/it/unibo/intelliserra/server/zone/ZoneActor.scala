package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.common.utils.Utils._
import it.unibo.intelliserra.core.action.{Action, DoingActions, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.perception.Measure
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.RepeatedAction
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.entityManager.DeviceChannel
import it.unibo.intelliserra.server.rule.RuleEngineService
import it.unibo.intelliserra.server.zone.ZoneActor.ComputeState

import scala.concurrent.duration.{FiniteDuration, _}

private[zone] class ZoneActor(private val aggregators: List[Aggregator],
                              override val rate : FiniteDuration,
                              val computeActionsRate : FiniteDuration)
                              extends Actor
                              with RepeatedAction[ComputeState]
                              with ActorLogging{

  context.actorOf(Props(RuleCheckerActor(computeActionsRate, s"../../${RuleEngineService.name}")))

  override val repeatedMessage: ComputeState = ComputeState()

  private[zone] var state : Option[State] = None
  private[zone] var sensorsValue: Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities: Set[DeviceChannel] = Set()
  private[zone] var actuatorsState: Map[ActorRef, OperationalState] = Map()

  override def receive: Receive = {
    case AddEntity(entityChannel) =>
      associatedEntities += entityChannel
      log.info(s"entity ${entityChannel.device.identifier} added to zone ${self.path.name}")
    case DeleteEntity(entityChannel) =>
      associatedEntities -= entityChannel
      log.info(s"entity ${entityChannel.device.identifier} removed to zone ${self.path.name}")
    case GetState => sender ! MyState(state)
    // TODO: the best solution? I think no
    case DoActions(actions) =>
      log.info(s"inferred actions: $actions")
      associatedEntities.map { c => (c.channel, actions.filter(actionToDo => c.device.capability.includes(Capability.acting(actionToDo.getClass))))}
        .filter(_._2.nonEmpty)
        .foreach { case (actuatorRef, actionsToDo) =>
          log.info(s"the zone asks the actuator ${actuatorRef.path.name} to perform the following action:${actionsToDo}")
          actuatorRef ! DoActions(actionsToDo)
        }

    case SensorMeasureUpdated(measure) =>
      sensorsValue += sender -> measure
      log.info(s"zone update value for sensor ${sender.path.name}; new value: $measure")
    case ComputeState() =>
      state = Option(computeState())
      sensorsValue = Map()
      log.info(s"state updated for zone ${sender.path.name}; new zone state: ${state.get}")
    case ActuatorStateChanged(operationalState) =>
      actuatorsState += sender -> operationalState
      log.info(s"zone update state for actuator ${sender.path.name}; new actuator state: $operationalState")
  }

  private[zone] def computeAggregatedPerceptions() : List[Measure] = {
    val measuresTry = for {
      (category, measures) <- sensorsValue.values.groupBy(_.category)
      aggregator <- aggregators.find(_.category == category)
    } yield aggregator.aggregate(measures.toList)
    flattenIterableTry(measuresTry)(e => log.error(e,"incompatible measures type"))(identity).toList
  }

  private[zone] def computeActuatorState() : List[Action] = actuatorsState.values.flatMap({
    case DoingActions(actions) => actions
    case Idle => Nil
  }).toList.distinct

  private[zone] def computeState() : State = {
    State(computeAggregatedPerceptions(), computeActuatorState())
  }

}

object ZoneActor {
  case class ComputeState()
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
    require(atMostOne(aggregators)(_.category), "only one aggregator must be assigned for each category")
    Props(new ZoneActor(aggregators, computeStateRate, computeActionsRate))
  }

}
