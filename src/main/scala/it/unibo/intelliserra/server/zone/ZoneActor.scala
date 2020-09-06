package it.unibo.intelliserra.server.zone

import java.sql.Date
import java.time.{LocalDateTime, ZoneId}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.{ActuatorStateChanged, AddEntity, DeleteEntity, DoActions, GetState, MyState, SensorMeasureUpdated}
import it.unibo.intelliserra.core.actuator.{Action, DoingActions, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.{ActingCapability, EntityChannel, RegisteredActuator}
import it.unibo.intelliserra.core.sensor.{Category, Measure}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.ActorWithRepeatedAction
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.common.utils.Utils._
import it.unibo.intelliserra.server.zone.ZoneActor.ComputeState

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

// TODO: default in constructor or only in apply
private[zone] class ZoneActor(private val aggregators: List[Aggregator],
                              override val rate : FiniteDuration = ZoneActor.defaultStateEvaluationRate,
                              val computeActionsRate : FiniteDuration = ZoneActor.defaultActionsEvaluationRate)
                              extends Actor with ActorWithRepeatedAction[ComputeState] with ActorLogging{

  context.actorOf(Props[RuleCheckerActor])

  override val repeatedMessage: ComputeState = ComputeState()

  private[zone] var state : Option[State] = None
  private[zone] var sensorsValue: Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities: Set[EntityChannel] = Set()
  private[zone] var actuatorsState: Map[ActorRef, OperationalState] = Map()

  override def receive: Receive = {
    case AddEntity(entityChannel) => associatedEntities += entityChannel
    case DeleteEntity(entityChannel) => associatedEntities -= entityChannel
    case GetState => sender ! MyState(state)
    case DoActions(actions) => associatedEntities.flatMap{
                                  case EntityChannel(RegisteredActuator(_,ActingCapability(actingCapabilities)),actuatorRef) =>
                                      Set((actuatorRef, actions.intersect(actingCapabilities)))
                                  case _ =>  None
                                }.filter(_._2.nonEmpty)
                                .foreach({ case (actuatorRef, actionsToDo) => actuatorRef ! DoActions(actionsToDo) })

    case SensorMeasureUpdated(measure) => sensorsValue += sender -> measure
    case ComputeState => state = Option(computeState()) ; sensorsValue = Map()
    case ActuatorStateChanged(operationalState) => actuatorsState += sender -> operationalState
  }

  private[zone] def computeAggregatedPerceptions() : List[Measure] = {
    val measuresTry = for {
      (category, measures) <- sensorsValue.values.groupBy(_.category)
      aggregator <- aggregators.find(_.category == category)
    } yield aggregator.aggregate(measures.toList)
    flattenIterableTry(measuresTry)(e => log.error(e,"incompatible measures type"))(identity).toList
  }

  private[zone] def computeActuatorState() : List[Action] = actuatorsState.values.flatMap({
    case DoingActions(action) => action
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

  def apply(name: String, aggregators: List[Aggregator],
            computeStateRate : FiniteDuration = defaultStateEvaluationRate,
            computeActionsRate : FiniteDuration = defaultActionsEvaluationRate)
           (implicit system: ActorSystem): ActorRef = {
    require(atMostOne(aggregators)(_.category), "only one aggregator must be assigned for each category")
    system actorOf (Props(new ZoneActor(aggregators, computeStateRate, computeActionsRate)), name)
  }

}
