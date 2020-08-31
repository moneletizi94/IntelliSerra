package it.unibo.intelliserra.server.zone

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import it.unibo.intelliserra.common.communication.Messages._
import it.unibo.intelliserra.core.entity.EntityChannel

/**
 * This is the Zone Manager actor which is in charge to create new zone actors when
 * a client needs them, it keeps link between zone identifier (given by the client)
 * and the actor ref. It also be able to delete zones and check for zone existence
 * It manages link between entities (sensors and actuators) and zones
 */

private[zone] class ZoneManagerActor extends Actor with ActorLogging {

  implicit val system: ActorSystem = context.system
  /* It keeps links between zones names and zones ActorRefs */
  var zones: Map[String, ActorRef] = Map()
  /* It keeps assigned Entities, there are all the zones kept in zones structure, but only the non-pending entities */
  var assignedEntities: Map[String, Set[EntityChannel]] = Map() //zone, entityChannel
  /* It keeps pending entities: a request for assignment has come but entity hasn't responded yet.
  * It only keeps zones for which there are pending requests.
  * */
  var pending: Map[String, Set[EntityChannel]] = Map()

  override def receive : Receive = {
    case CreateZone(zoneID) => sender() ! onCreateZone(zoneID)

    case RemoveZone(zoneID) => sender() ! onRemoveZone(zoneID)

    case GetZones => sender() ! ZonesResult(zones.keySet.toList)

    case AssignEntityToZone(zoneID, entityChannel) => sender() ! onAssignEntity(zoneID, entityChannel)

    case DissociateEntityFromZone(entityChannel) => sender() ! onDissociateEntity(entityChannel)

    case Ack => onAck()
  }

  /* --- ON RECEIVE ACTIONS --- */
  private def onCreateZone(zoneID: String): ZoneManagerResponse = {
    zones.get(zoneID) match {
      case Some(_) => ZoneAlreadyExists
      case None =>
        zones += zoneID -> createZoneActor(zoneID)
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

  private def onAssignEntity(zoneID: String, entityChannel: EntityChannel): ZoneManagerResponse = {
    zones.get(zoneID).fold[ZoneManagerResponse](ZoneNotFound)(_ => {
      assignedEntities.find({case (_, set) => set.contains(entityChannel)}) match {
        case Some((zone, _)) => AlreadyAssigned(zone)
        case None =>
          pending.find({ case (_, set) => set.contains(entityChannel) })
            .foreach({case (zone, set) => removeFromPending(entityChannel, zone, set)})
          pending += (zoneID -> (pending.getOrElse(zoneID, Set()) + entityChannel))
          entityChannel.channel ! AssociateTo(zones(zoneID), zoneID)
          AssignOk
      }
    })
  }

  private def onDissociateEntity(entityChannel: EntityChannel): ZoneManagerResponse = {
    assignedEntities.find({case (_, set) => set.contains(entityChannel)}) match {
      case Some((zoneID, entities)) =>
        zones(zoneID) ! DeleteEntity(entityChannel) //if the zone exists in zones, it will exists also in assignedEntities
        assignedEntities += (zoneID -> entities.filter(_!= entityChannel))
        informEntityToDissociate(entityChannel, zoneID)
      case None =>
        pending.find({case (_, set) => set.contains(entityChannel)})
          .fold[ZoneManagerResponse](AlreadyDissociated)({
          case (zoneID, entities) =>
            removeFromPending(entityChannel, zoneID, entities)
            informEntityToDissociate(entityChannel, zoneID)
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

  /* --- UTILITY METHODS ---*/

  //This is done to override the creation of an actor to test it
  private[zone] def createZoneActor(zoneID: String ): ActorRef = ZoneActor(zoneID, List())

  private def deleteZoneFromStructuresAndInformEntities(zoneID: String): Unit = {
    informEntitiesToDissociate(assignedEntities(zoneID), zoneID) //if the zone exists in zones, it will exists also in assignedEntities
    pending.get(zoneID).foreach(set => {
      informEntitiesToDissociate(set, zoneID)
      pending -= zoneID
    })
    zones -= zoneID
    assignedEntities -= zoneID
  }

  private def informEntitiesToDissociate(entities: Set[EntityChannel], zoneID: String): Unit = {
    entities.foreach(entityChannel => informEntityToDissociate(entityChannel, zoneID))
  }
  private def informEntityToDissociate(entityChannel: EntityChannel, zoneID: String): ZoneManagerResponse = {
    entityChannel.channel ! DissociateFrom(zones(zoneID), zoneID)
    DissociateOk
  }

  private def removeFromPending(entityToRemove: EntityChannel, zoneID: String, entities: Set[EntityChannel]): Unit = {
    entities.filter(_ != entityToRemove) match {
      case set if set.isEmpty => pending -= zoneID
      case set => pending += (zoneID -> set)
    }
  }
}

object ZoneManagerActor {
  val name = "ZoneManager"
  def apply()(implicit actorSystem: ActorSystem): ActorRef = actorSystem actorOf (Props[ZoneManagerActor](), name)
}