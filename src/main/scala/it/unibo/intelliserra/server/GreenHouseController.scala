package it.unibo.intelliserra.server

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.communication.Messages
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.zone.ZoneManagerActor
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

private[server] class GreenHouseController extends Actor with ActorLogging {

  private implicit val system: ActorSystem = context.system
  private implicit val timeout: Timeout = Timeout(5 seconds)
  private val zoneManagerActor: ActorRef = ZoneManagerActor()
  private val entityManagerActor: ActorRef = EntityManagerActor()
  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {

    case CreateZone(zoneName) =>
      zoneManagerActor ? Messages.CreateZone(zoneName) onComplete  {
        case Messages.ZoneCreated => sender ! Created
        case Messages.ZoneAlreadyExists => Future.successful(sender ! Conflict)
      }
      /*case CreateZone(zoneName) =>
      createRequest(Messages.CreateZone(zoneName)){
        case Messages.ZoneCreated => Success(sender ! Created )
        case Messages.ZoneAlreadyExists => Success(sender ! Conflict)
      }*/
      zoneManagerActor ? Messages.CreateZone(zoneName) flatMap {
        case Messages.ZoneCreated => Future.successful(sender ! Created)
        case Messages.ZoneAlreadyExists => Future.successful(sender ! Conflict)
      }

    case DeleteZone(zoneName) =>
      zoneManagerActor ? Messages.RemoveZone(zoneName) flatMap {
        case Messages.ZoneRemoved => Future.successful(sender ! Deleted)
        case Messages.ZoneNotFound => Future.successful(sender ! NotFound)
      }

    case GetZones() => zoneManagerActor ? Messages.GetZones flatMap {
      case Messages.ZonesResult(zones) =>
        if (zones.nonEmpty) Future.successful(sender ! ServiceResponse(Ok, zones)) else Future.successful(sender ! ServiceResponse(NotFound, "No zones!"))
    }

    case AssignEntity(zoneName, entityId) =>
      (entityManagerActor ? Messages.GetEntity(entityId)) flatMap {
        case Messages.EntityResult(entity) => zoneManagerActor ? Messages.AssignEntityToZone(zoneName, entity) flatMap {
          case Messages.ZoneNotFound => Future.successful(sender ! NotFound)
          case Messages.AlreadyAssigned(zone) => Future.successful(sender ! ServiceResponse(Conflict, zone))
          case Messages.AssignOk => Future.successful(sender ! Ok)
          case Messages.AssignError(error) => Future.successful(sender ! ServiceResponse(Error, error))
        }
        case Messages.EntityNotFound => Future.successful(sender ! NotFound)
      }
    /* for {
        entity <- (entityManagerActor ? EntityExists(idEntity)).asInstanceOf[Future[Option[(ActorRef, RegisteredEntity)]]]
        zoneOption <- (zoneManagerActor ? ZoneExists(zone)).asInstanceOf[Future[Option[ActorRef]]]
        if zoneOption.isDefined && entity.isDefined
        _ <- zoneOption.get ? AssignEntity(entity.get._1, entity.get._2)
      } yield sender ! AssignOk*/

    case DissociateEntity(entityId) =>
      (entityManagerActor ? Messages.GetEntity(entityId)) flatMap {
        case Messages.EntityResult(entity) => zoneManagerActor ? Messages.DissociateEntityFromZone(entity) flatMap {
          case Messages.DissociateOk => Future.successful(sender ! Ok)
          case Messages.AlreadyDissociated => Future.successful(sender ! Error)
        }
        case Messages.EntityNotFound => Future.successful(sender ! NotFound)
      }

    case RemoveEntity(entityId) => entityManagerActor ? Messages.RemoveEntity(entityId) flatMap {
      case Messages.EntityNotFound => Future.successful(sender ! NotFound)
      case Messages.EntityRemoved => Future.successful(sender ! Deleted)
    }
  }

  /*private def createRequest(msg: => Any)(responseTransform: Any => Try[Any]) : Unit = {
    zoneManagerActor ? msg flatMap{ msg => Future.fromTry(responseTransform(msg)) } pipeTo sender()
  }*/
}

object GreenHouseController {
  val name = "GreenHouseController"

  def apply()(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf(Props[GreenHouseController], name)
}


