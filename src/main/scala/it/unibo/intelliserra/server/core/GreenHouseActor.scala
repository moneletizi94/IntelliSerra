package it.unibo.intelliserra.server.core

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.actor.DefaultExecutionContext
import it.unibo.intelliserra.common.communication.Protocol.ServiceResponse
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.ServerConfig.{RuleConfig, ZoneConfig}
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.{GreenHouseController, ServerConfig}
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}
import it.unibo.intelliserra.server.entityManager.{EMEventBus, EntityManagerActor}
import it.unibo.intelliserra.server.rule.RuleEngineService
import it.unibo.intelliserra.server.zone.ZoneManagerActor

import scala.concurrent.duration._
import scala.util.Try

private[core] object GreenHouseActor {

  sealed trait ServerCommand

  /**
   * Start the server
   */
  case object Start extends ServerCommand

  /**
   * Responses to server commands
   */
  sealed trait ServerResponse
  case object Started extends ServerResponse
  final case class ServerError(throwable: Throwable) extends ServerResponse

  /**
   * Create a green house server actor
   * @param actorSystem the actor system for create the actor
   * @param ruleConfig rules configuration for rule engine service
   * @param zoneConfig zones configuration for zone manager
   * @return an actor ref of green house server actor
   */
  def apply(ruleConfig: RuleConfig, zoneConfig: ZoneConfig)(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem actorOf (Props(new GreenHouseActor(ruleConfig, zoneConfig)), name = "serverActor")
  }
}

private[core] class GreenHouseActor(ruleConfig: RuleConfig, zoneConfig: ZoneConfig) extends Actor with DefaultExecutionContext {

  type ResponseMap[T] = PartialFunction[Try[T], ServiceResponse]

  private implicit val actorSystem: ActorSystem = context.system
  private implicit val timeout: Timeout = Timeout(5 seconds)

  var greenHouseController: ActorRef = _
  var zoneManagerActor: ActorRef = _
  var entityManagerActor: ActorRef = _
  var ruleEngineService: ActorRef = _

  private def idle: Receive = {
    case Start =>
      zoneManagerActor = ZoneManagerActor(zoneConfig)
      entityManagerActor = EntityManagerActor()
      ruleEngineService = RuleEngineService(ruleConfig.rules)
      EMEventBus.subscribe(zoneManagerActor, EMEventBus.topic) //it will update zoneManager on removeEntity
      greenHouseController = GreenHouseController(zoneManagerActor, entityManagerActor)
      context.become(running orElse routeToController)
      sender ! Started
  }

  private def running: Receive = {
    case Start => sender ! ServerError(new IllegalStateException("Server is already running"))
  }

  def routeToController: Receive = {
    case request : ClientRequest  => greenHouseController.tell(request, sender())
  }

  override def receive: Receive = idle
}