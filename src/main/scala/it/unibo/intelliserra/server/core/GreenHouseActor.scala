package it.unibo.intelliserra.server.core

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import it.unibo.intelliserra.common.akka.actor.{DefaultExecutionContext, DefaultTimeout}
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.GreenHouseController
import it.unibo.intelliserra.server.ServerConfig.{RuleConfig, ZoneConfig}
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started, _}
import it.unibo.intelliserra.server.entityManager.{EMEventBus, EntityManagerActor}
import it.unibo.intelliserra.server.rule.RuleEngineService
import it.unibo.intelliserra.server.zone.{ZoneActor, ZoneManagerActor}

private[core] object GreenHouseActor {

  sealed trait ServerCommand

  /**
   * Start the server
   */
  case object Start extends ServerCommand

  /**
   * Stop the server
   */
  case object Stop extends ServerCommand

  /**
   * Responses to server commands
   */
  sealed trait ServerResponse
  case object Started extends ServerResponse
  case object Stopped extends ServerResponse
  final case class ServerError(throwable: Throwable) extends ServerResponse

  /**
   * Create a green house server actor
   * @param actorSystem the actor system for create the actor
   * @param ruleConfig rules configuration for rule engine service
   * @param zoneConfig zones configuration for zone manager
   * @return an actor ref of green house server actor
   */
  def apply(ruleConfig: RuleConfig, zoneConfig: ZoneConfig)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem actorOf (props(ruleConfig, zoneConfig), name = "serverActor")
  }

  /**
   * Create the actor props for server actor.
   * @param ruleConfig  rules configuration for rule engine service
   * @param zoneConfig  zones configuration for zone manager
   * @return an actor ref of green house server actor
   */
  def props(ruleConfig: RuleConfig, zoneConfig: ZoneConfig): Props = Props(new GreenHouseActor(ruleConfig, zoneConfig))
}

private[core] class GreenHouseActor(ruleConfig: RuleConfig, zoneConfig: ZoneConfig) extends Actor
  with ActorLogging
  with DefaultTimeout
  with DefaultExecutionContext {

  private implicit val actorSystem: ActorSystem = context.system

  private var greenHouseController: ActorRef = _
  private var zoneManagerActor: ActorRef = _
  private var entityManagerActor: ActorRef = _
  private var ruleEngineServiceActor: ActorRef = _

  private def idle: Receive = {
    case Start =>
      log.info("GreenHouse started")
      entityManagerActor = EntityManagerActor()
      zoneManagerActor = ZoneManagerActor(zoneName => ZoneActor(zoneName, zoneConfig))
      ruleEngineServiceActor = RuleEngineService(ruleConfig.rules)
      EMEventBus.subscribe(zoneManagerActor, EMEventBus.topic)
      greenHouseController = GreenHouseController(zoneManagerActor, entityManagerActor, ruleEngineServiceActor)
      context.become(running)
      sender ! Started
    case Stop =>
      log.info("GreenHouse stopped")
      sender ! ServerError(new IllegalStateException("Server is not running"))
  }

  private def running: Receive = {
    case Start => sender ! ServerError(new IllegalStateException("Server is already running"))
    case request: ClientRequest  => greenHouseController.tell(request, sender())
    case Stop => shutdownActors(sender, List(greenHouseController, ruleEngineServiceActor, zoneManagerActor, entityManagerActor))
  }

  private def terminating(actors: List[ActorRef], replyTo: ActorRef): Receive = actors match {
    case Nil => replyTo ! Stopped; idle
    case _ => {
      case Terminated(actorRef) =>
        context.become(terminating(actors.filterNot(_ == actorRef), replyTo))
    }
  }

  // Shutdown the actors and wait for termination
  private def shutdownActors(replyTo: ActorRef, actors: List[ActorRef]): Unit = {
    actors.map(context.watch).foreach(context stop)
    context.become(terminating(actors, replyTo))
  }

  override def receive: Receive = idle
}