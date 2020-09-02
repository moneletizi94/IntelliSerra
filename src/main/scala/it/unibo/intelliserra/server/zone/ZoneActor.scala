package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Timers}
import it.unibo.intelliserra.common.communication.Messages.{AddEntity, DeleteEntity, DoActions, GetState, MyState, SensorMeasure}
import it.unibo.intelliserra.core.actuator.{Action, DoingAction, Idle, OperationalState}
import it.unibo.intelliserra.core.entity.EntityChannel
import it.unibo.intelliserra.core.sensor.{Category, Measure}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.ActorWithRepeatedAction
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.common.utils.Utils._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

private[zone] class ZoneActor(private val aggregators: List[Aggregator],
                              override val rate : FiniteDuration = ZoneActor.defaultTickRate)
                              extends Actor with ActorWithRepeatedAction with ActorLogging with Timers{

  private[zone] var state : Option[State] = None
  private[zone] var sensorsValue: Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities: Set[EntityChannel] = Set()
  private[zone] var actuatorsState: Map[ActorRef, OperationalState] = Map()

  override def receive: Receive = {
    case AddEntity(entityChannel) => associatedEntities += entityChannel
    case DeleteEntity(entityChannel) => associatedEntities -= entityChannel
    case GetState => sender ! MyState(state)
    case DoActions(actions) =>
    /*associatedActuators.map(actuator => (actuator._1,actuator._2.capabilities.actions.intersect(actions)))
                        .filter(_._2.nonEmpty)
                        //.flatMap()*/
    case SensorMeasure(measure) => sensorsValue += sender -> measure
    case Tick =>
  }

  private[zone] def computeAggregatedPerceptions() : List[Measure] = {
    val measuresTry = for {
      (category, measures) <- sensorsValue.values.groupBy(_.category)
      aggregator <- aggregators.find(_.category == category)
    } yield aggregator.aggregate(measures.toList)
    println(measuresTry)
    flattenIterableTry(measuresTry)(e => log.error(e,""))(identity).toList
  }

  private[zone] def computeActuatorState() : List[Action] = actuatorsState.values.flatMap({
    case DoingAction(action) => List(action)
    case Idle => Nil
  }).toList.distinct

  private[zone] def computeState() : Option[State] = Option(State(computeAggregatedPerceptions(), computeActuatorState()))

}

object ZoneActor {
  private val defaultTickRate = 10 seconds

  def apply(name: String, aggregators: List[Aggregator],rate : FiniteDuration = defaultTickRate)(implicit system: ActorSystem): ActorRef = {
    require(atMostOne(aggregators)(_.category), "only one aggregator must be assigned for each category")
    system actorOf (Props(new ZoneActor(aggregators, rate)), name)
  }

}
