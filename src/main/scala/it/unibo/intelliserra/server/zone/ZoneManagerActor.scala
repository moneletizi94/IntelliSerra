package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.server.entityManager.DeviceChannel
import it.unibo.intelliserra.server.entityManager.EMEventBus.PublishedOnRemoveEntity

/*
 * This is the Zone Manager actor which is in charge to create new zone actors when
 * a client needs them, it keeps link between zone identifier (given by the client)
 * and the actor ref. It also be able to delete zones and check for zone existence.
 * It manages link between entities (sensors and actuators) and zones.
 */

private[zone] class ZoneManagerActor(private val zoneStrategy: String => ActorRef) extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  /* It keeps links between zones names and zones ActorRefs */
  var zones: Map[String, ActorRef] = Map()
  /* It keeps assigned Entities, there are all the zones kept in zones structure, but only the non-pending entities */
  var assignedEntities: Map[String, Set[DeviceChannel]] = Map() //zone, entityChannel
  /* It keeps pending entities: a request for assignment has come but entity hasn't responded yet.
  * It only keeps zones for which there are pending requests.
  * */
  var pending: Map[String, Set[DeviceChannel]] = Map()

  override def receive : Receive = {
    case CreateZone(zoneID) => sender() ! onCreateZone(zoneID)

    case RemoveZone(zoneID) => sender() ! onRemoveZone(zoneID)

    case GetZones => sender() ! ZonesResult(zones.keySet.toList)

    case AssignEntityToZone(zoneID, entityChannel) => sender() ! onAssignEntity(zoneID, entityChannel)

    case DissociateEntityFromZone(entityChannel) => sender() ! onDissociateEntity(entityChannel)

    case Ack => onAck()

    case PublishedOnRemoveEntity(entityChannel) => onDissociateEntity(entityChannel)

    case GetZoneState(zoneID) => onGetZoneState(zoneID)
  }

  /* --- ON RECEIVE ACTIONS --- */
  private def onCreateZone(zoneID: String): ZoneManagerResponse = {
    zones.get(zoneID) match {
      case Some(_) => ZoneAlreadyExists
      case None =>
        zones += zoneID -> zoneStrategy(zoneID)
        assignedEntities += zoneID -> Set()
        ZoneCreated
    }
  }

  private def onRemoveZone(zoneID: String): ZoneManagerResponse = {
    zones.get(zoneID).fold[ZoneManagerResponse](ZoneNotFound)(_ => {
      zones(zoneID) ! PoisonPill
      deleteZoneFromStructuresAndInformEntities(zoneID)
      ZoneRemoved
    })
  }

  private def onAssignEntity(zoneID: String, entityChannel: DeviceChannel): ZoneManagerResponse = {
    zones.get(zoneID).fold[ZoneManagerResponse](ZoneNotFound)(_ => {
      assignedEntities.find({case (_, set) => set.contains(entityChannel)}) match {
        case Some((zone, _)) => AlreadyAssigned(zone)
        case None =>
          pending.find({ case (_, set) => set.contains(entityChannel) })
            .foreach({case (zone, set) => removeFromPending(entityChannel, zone, set)})
          pending += (zoneID -> (pending.getOrElse(zoneID, Set()) + entityChannel))
          answerAndInformEntityTo(entityChannel.channel, AssignOk, AssociateTo(zones(zoneID),zoneID))
      }
    })
  }

  private def onDissociateEntity(entityChannel: DeviceChannel): ZoneManagerResponse = {
    assignedEntities.find({case (_, set) => set.contains(entityChannel)}) match {
      case Some((zoneID, entities)) =>
        zones(zoneID) ! DeleteEntity(entityChannel) //if the zone exists in zones, it will exists also in assignedEntities
        assignedEntities += (zoneID -> entities.filter(_!= entityChannel))
        answerAndInformEntityTo(entityChannel.channel, DissociateOk, DissociateFrom(zones(zoneID), zoneID))
      case None =>
        pending.find({case (_, set) => set.contains(entityChannel)})
          .fold[ZoneManagerResponse](AlreadyDissociated)({
          case (zoneID, entities) =>
            removeFromPending(entityChannel, zoneID, entities)
            answerAndInformEntityTo(entityChannel.channel, DissociateOk, DissociateFrom(zones(zoneID), zoneID))
        })
    }
  }

  private def onAck(): Unit = {
    pending.find({case (_, set) => set.exists(_.channel == sender())}).foreach({case (zoneID, entities) =>
      val entityToMove = entities.head
      zones(zoneID) ! AddEntity(entityToMove)
      assignedEntities = assignedEntities + (zoneID -> (assignedEntities(zoneID) + entityToMove))
      removeFromPending(entityToMove ,zoneID, entities)
    })
  }

  private def onGetZoneState(zoneID: String): Unit = {
    zones.find(zone => zone._1 == zoneID).fold(sender ! ZoneNotFound)(zone => {
      zone._2.tell(GetState, sender())
    })
  }

  /* --- UTILITY METHODS ---*/

  private def deleteZoneFromStructuresAndInformEntities(zoneID: String): Unit = {
    informEntitiesToDissociate(assignedEntities(zoneID), zoneID) //if the zone exists in zones, it will exists also in assignedEntities
    pending.get(zoneID).foreach(set => {
      informEntitiesToDissociate(set, zoneID)
      pending -= zoneID
    })
    zones -= zoneID
    assignedEntities -= zoneID
  }

  private def informEntitiesToDissociate(entities: Set[DeviceChannel], zoneID: String): Unit = {
    entities.foreach(entityChannel =>
      answerAndInformEntityTo(entityChannel.channel, DissociateOk, DissociateFrom(zones(zoneID),zoneID)))
  }

  private def answerAndInformEntityTo(entity: ActorRef, answer: ZoneManagerResponse, information: EntityRequest): ZoneManagerResponse = {
    entity ! information
    answer
  }

  private def removeFromPending(entityToRemove: DeviceChannel, zoneID: String, entities: Set[DeviceChannel]): Unit = {
    entities.filter(_ != entityToRemove) match {
      case set if set.isEmpty => pending -= zoneID
      case set => pending += (zoneID -> set)
    }
  }
}

object ZoneManagerActor {
  val name = "ZoneManager"

  /**
   * Creates a zoneManagerActor responsible for managing zones
    * @param zoneStrategy, a strategy used to specify how to create a
   *                    [[it.unibo.intelliserra.server.zone.ZoneActor]] given a name
   * @param actorSystem, the actor system to create the actor
   * @return
   */
  def apply(zoneStrategy: String => ActorRef)(implicit actorSystem: ActorSystem): ActorRef =
    actorSystem actorOf (Props(new ZoneManagerActor(zoneStrategy)), name)
}