package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

private[zone] class ZoneActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ => "So Fake"
  }
/*
  //var List[Aggregator] = Aggregators
  private[zone] var sensorsValue : Map[ActorRef, Measure] = Map()
  private[zone] var associatedEntities : Map[ActorRef, EntityChannel] = Map()
  private[zone] var actuatorsState : Map[ActorRef, OperationalState] = Map()

  implicit val timeout : Timeout = Timeout(5 seconds)
  implicit val ec: ExecutionContext = context.dispatcher
  // TODO: do BaseActor 

  override def receive: Receive = {
    case DestroyYourself =>
      associatedEntities.keySet.foreach(entity => entity ! DissociateFromMe(self))
      context stop self
    case AddEntity(entityChannel) =>
      //entityRef ? AssociateToMe(self) map { case Ack => GetEntityInfo(sender() , entityRef, entity) } pipeTo self
    case DeleteEntity(entityChannel) => entityChannel.channel ! DissociateFromMe(self)
    case Tick =>
      /*sensorsValue.values.groupBy(measure => (measure.category, measure))
                          .map({case (category, measures) => state = (category, /*aggregators.aggregate(measures)*/measures)})*/
    case DoActions(actions) =>
      /*associatedActuators.map(actuator => (actuator._1,actuator._2.capabilities.actions.intersect(actions)))
                          .filter(_._2.nonEmpty)
                          //.flatMap()*/
  }


  private case object Tick
  private case class DoActions(actions : Set[Action])*/
}

object ZoneActor {
  def apply(name: String)(implicit system: ActorSystem): ActorRef = system actorOf (Props[ZoneActor], name)
}
