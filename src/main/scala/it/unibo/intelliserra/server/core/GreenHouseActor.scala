package it.unibo.intelliserra.server.core

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.actor.DefaultExecutionContext
import it.unibo.intelliserra.common.communication.Messages
import it.unibo.intelliserra.common.communication.Messages.{ZoneAlreadyExists, ZoneCreated, ZoneNotFound, ZoneRemoved, ZonesResult}
import it.unibo.intelliserra.common.communication.Protocol.{ClientRequest, Conflict, CreateZone, Created, DeleteZone, Deleted, Error, GetZones, NotFound, Ok, ServiceResponse}
import it.unibo.intelliserra.server.{EntityManagerActor, GreenHouseController}
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}
import it.unibo.intelliserra.server.zone.ZoneManagerActor

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

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
   * @return an actor ref of green house server actor
   */
  def apply()(implicit actorSystem: ActorSystem): ActorRef = {
    actorSystem actorOf (Props[GreenHouseActor], name = "serverActor")
  }
}

private[core] class GreenHouseActor extends Actor with DefaultExecutionContext {

  type ResponseMap[T] = PartialFunction[Try[T], ServiceResponse]

  private implicit val actorSystem: ActorSystem = context.system
  private implicit val timeout: Timeout = Timeout(5 seconds)

  var greenHouseController: ActorRef = _
  var zoneManagerActor: ActorRef = _
  var entityManagerActor: ActorRef = _

  private def idle: Receive = {
    case Start =>
      zoneManagerActor = ZoneManagerActor()
      entityManagerActor = EntityManagerActor()
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

  private def sendResponseWithFallback[T](request: Future[T], replyTo: ActorRef)(mapSend: ResponseMap[T]): Unit = {
    request onComplete { sendResponseWithFallback(replyTo)(mapSend) }
  }

  private def sendResponse[T](replyTo: ActorRef)(mapSend: ResponseMap[T]): Try[T] => Unit = replyTo ! mapSend(_)
  private def sendResponseWithFallback[T](replyTo: ActorRef)(mapSend: ResponseMap[T]): Try[T] => Unit = sendResponse(replyTo)(mapSend orElse fallback)

  private def fallback[T]: ResponseMap[T] = {
    case Failure(exception) => ServiceResponse(Error, exception.toString)
    case _ => ServiceResponse(Error, "Internal Error")
  }

  override def receive: Receive = idle
}