package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.core.RegisteredEntity
import it.unibo.intelliserra.server.zone.ZoneManagerActor

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

private[server] class GreenHouseController extends Actor with ActorLogging {

  private implicit val system: ActorSystem = context.system
  private implicit val timeout: Timeout = Timeout(5 seconds)
  private var zoneManagerActor: ActorRef = ZoneManagerActor()
  private var entityManagerActor: ActorRef = EntityManagerActor()
  private var request: String = _

  override def receive: Receive = receiveEntityRequest


  implicit val ec: ExecutionContext = context.dispatcher

  def receiveEntityRequest: Receive = {
    case Assign(idEntity, zone) =>
      /* (entityManagerActor ? EntityExists(idEntity)).asInstanceOf[Future[Option[(ActorRef, RegisteredEntity)]]] flatMap {
         case None => Future.failed(new Exception("no entity"))
         case Some((entityRef, registeredEntity)) => (zoneManagerActor ? ZoneExists(zone)).asInstanceOf[Future[Option[ActorRef]]] map {
           case Some(value) => (value ? AssignSensor(entityRef, registeredEntity)).asInstanceOf[Future[ZoneResponse]] pipeTo sender()
           case None => Future.failed(new Exception("no"))

      }*/
      for {
        entity <- (entityManagerActor ? EntityExists(idEntity)).asInstanceOf[Future[Option[(ActorRef, RegisteredEntity)]]]
        zoneOption <- (zoneManagerActor ? ZoneExists(zone)).asInstanceOf[Future[Option[ActorRef]]]
        if zoneOption.isDefined && entity.isDefined
        //zoneRef <- zoneOption.get ? AssignEntity(entity.get._1, entity.get._2)
      } yield sender ! ZoneResponse

    case Dissociate(idEntity, zone) =>
      for {
        entity <- (entityManagerActor ? EntityExists(idEntity)).asInstanceOf[Future[Option[(ActorRef, RegisteredEntity)]]]
        zoneOption <- (zoneManagerActor ? ZoneExists(zone)).asInstanceOf[Future[Option[ActorRef]]]
        if zoneOption.isDefined && entity.isDefined
        //zoneRef <- zoneOption.get ? DissociateEntity(entity.get._1)
      } yield sender ! ZoneResponse
  }

  /*def receiveZoneResponse(sender: ActorRef): Receive = {
    case ZoneResponse => sender ! ZoneResponse
  }

  def checked(idEntity: String, zone: String, toSend: (ActorRef, ActorRef, RegisteredEntity) => Unit): Unit = {
    zoneManagerActor ? ZoneExists(zone)
    context.become(receiveCheckedZone(idEntity, sender(), toSend))
  }

  def receiveCheckedZone(idEntity: String, sender: ActorRef, toSend: (ActorRef, ActorRef, RegisteredEntity) => Unit): Receive = {
    case Zone(zones) =>
      entityManagerActor ? EntityExists(idEntity)
      context.become(receiveCheckedEntity(zones, sender, toSend))
    case NoZone => sender ! NoZone
  }

  def receiveCheckedEntity(zones: ActorRef, sender: ActorRef, toSend: (ActorRef, ActorRef, RegisteredEntity) => Unit): Receive = {
    case Entity(actorRef, registeredEntity) =>
      toSend(zones, actorRef, registeredEntity)
      context.become(receiveEntityRequest)
    case NoEntity =>
      sender ! NoEntity
      context.become(receiveEntityRequest)
  }*/
}

object GreenHouseController {
  val name = "GreenHouseController"

  def apply()(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf(Props[GreenHouseController], name)
}


