package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import it.unibo.intelliserra.common.communication.Messages.{AddEntity, DeleteEntity, DoActions, GetState, MyState, SensorMeasure}
import it.unibo.intelliserra.core.actuator.{DoingAction, OperationalState}
import it.unibo.intelliserra.core.entity.EntityChannel
import it.unibo.intelliserra.core.sensor.{Category, Measure}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.ActorWithRepeatedAction
import it.unibo.intelliserra.server.aggregation.Aggregator

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

private[zone] class ZoneActor(private val aggregators: List[Aggregator],
                              override val rate : FiniteDuration = ZoneActor.defaultTickRate)
                              extends ActorWithRepeatedAction with ActorLogging {

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
  }

  private[zone] def computeAggregatedPerceptions() : List[Measure] = {
    val measuresTry = for {
      (category, measures) <- sensorsValue.values.groupBy(_.category)
      aggregator <- aggregators.find(_.category == category)
    } yield aggregator.aggregate(measures.toList)
    flattenIterableTry(measuresTry)(e => log.error(e,""))(identity).toList
  }

  // TODO: DoingAction??? functor? 
  private[zone] def computeActuatorState() : List[DoingAction] = actuatorsState.values.filter(_.isDoing()).map(_.asInstanceOf[DoingAction]).toList

  private[zone] def computeState() : Option[State] = Option(State(computeAggregatedPerceptions(), computeActuatorState()))

  override def onTick(): Unit = {
    state = computeState()
    sensorsValue = Map()
  }

  // TODO: in utility class?
  private[zone] def flattenIterableTry[A,B,C](iterable: Iterable[Try[B]])(ifFailure : Throwable => A)(ifSuccess : B => C): Iterable[C]  = {
    val (successes, failures) = iterable.partition(_.isSuccess)
    failures.map(_ => ifFailure)
    successes.flatMap(_.toOption).map(ifSuccess(_))
  }

}

object ZoneActor {
  private val defaultTickRate = 10 seconds

  def apply(name: String, aggregators: List[Aggregator])(rate : FiniteDuration = defaultTickRate)(implicit system: ActorSystem): ActorRef = {
    require(Aggregator.atMostOneCategory(aggregators), "only one aggregator must be assigned for each category")
    system actorOf (Props(new ZoneActor(aggregators, rate)), name)
  }
}
