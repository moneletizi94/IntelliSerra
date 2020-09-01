package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.entity.{ActingCapability, EntityChannel, SensingCapability}
import it.unibo.intelliserra.core.state.State

//noinspection ScalaStyle
object Messages {

  // Entity Manager Protocol
  sealed trait EntityManagerRequest
  sealed trait JoinRequest extends EntityManagerRequest
  final case class JoinSensor(identifier: String, sensingCapability: SensingCapability, sensorRef: ActorRef) extends JoinRequest
  final case class JoinActuator(identifier: String, actingCapability: ActingCapability, actuatorRef : ActorRef) extends JoinRequest
  final case class GetEntity(entityId: String) extends EntityManagerRequest
  final case class RemoveEntity(entityId: String) extends EntityManagerRequest
  // final case class Subscribe(observer: ActorRef) extends EntityManagerRequest

  sealed trait EntityManagerResponse
  sealed trait JoinResponse extends EntityManagerResponse
  case object JoinOK extends JoinResponse
  case class EntityResult(entity: EntityChannel) extends EntityManagerResponse
  case object EntityNotFound extends EntityManagerResponse
  case object EntityRemoved extends EntityManagerResponse
  case class JoinError(error: String) extends JoinResponse

  /* ZoneManagerProtocol (From GH to ZoneManager) */
  sealed trait ZoneManagerRequest
  //A client asks for a new Zone
  case class CreateZone(zoneName: String) extends ZoneManagerRequest
  //A client asks to remove a zone, the corresponding actor will be stopped
  case class RemoveZone(zoneName: String) extends ZoneManagerRequest
  //GH asks for all the zones in the ZoneManager
  case object GetZones extends ZoneManagerRequest
  case class AssignEntityToZone(zoneName: String, entityChannel: EntityChannel) extends ZoneManagerRequest
  case class DissociateEntityFromZone(entityChannel: EntityChannel) extends ZoneManagerRequest
  case class GetStateOfZone(zoneName: String) extends ZoneManagerRequest

  sealed trait ZoneManagerResponse
  case object ZoneCreated extends ZoneManagerResponse
  // Used to answer to RemoveZone
  case object ZoneRemoved extends ZoneManagerResponse
  case object ZoneAlreadyExists extends ZoneManagerResponse
  // Used to answer to RemoveZone (when the specified zone doesn't exists)
  case object ZoneNotFound extends ZoneManagerResponse
  //Used to Answer to GetZones
  case class ZonesResult(zones: List[String]) extends ZoneManagerResponse
  case class AlreadyAssigned(zone: String) extends ZoneManagerResponse
  case object AssignOk extends ZoneManagerResponse // From ZoneManager to GH
  case object DissociateOk extends ZoneManagerResponse // From ZoneManager to GH
  case class AssignError(error: String) extends ZoneManagerResponse
  case object AlreadyDissociated extends ZoneManagerResponse
  case object Ok extends ZoneManagerResponse

  // Zone Protocol (From ZoneManager to Zone)
  sealed trait ZoneRequest
  case class AddEntity(entityChannel: EntityChannel) extends ZoneRequest
  case class DeleteEntity(entityChannel: EntityChannel) extends ZoneRequest
  case object GetState extends ZoneRequest
  case class DoActions(actions: Set[Action]) extends ZoneRequest
  case class MyState(state : State)
  sealed trait EntityRequest
  case class DissociateFrom(zoneRef: ActorRef, zoneID: String) extends EntityRequest//From ZoneManager to Sensor/ Actuator
  case class AssociateTo(zoneRef: ActorRef, zoneID: String) extends EntityRequest//From ZoneManager to Sensor/ Actuator

  /* --- From Sensor/Actuator to ZoneManager --- */
  case object Ack

}
