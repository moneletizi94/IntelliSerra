package it.unibo.intelliserra.common.communication

import akka.actor.ActorRef
import it.unibo.intelliserra.core.action.{Action, OperationalState}
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.perception.Measure
import it.unibo.intelliserra.core.rule.RuleInfo
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.server.entityManager.DeviceChannel

/**
 * Object to describe all the messages exchanged on server-side with
 * a request-response fashion
 */
// scalastyle:off
object Messages {

  /**
   * Trait to represent requests forwarded to [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]]
   */
  sealed trait EntityManagerRequest


  /**
   * Message to join an actuator to [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]]
   * @param capability the capability of the specified device
   * @param deviceRef the actor ref of the specified device
   */

  final case class JoinDevice(identifier: String, capability: Capability, deviceRef: ActorRef)


  /**
   * Message sent to get an entity, if exists. It is used by the controller
   * @param entityId the identifier of the entity
   */
  final case class GetEntity(entityId: String) extends EntityManagerRequest

  /**
   * Message sent by the controller to remove an entity, if exists
   * @param entityId identifier of the entity to remove
   */
  final case class RemoveEntity(entityId: String) extends EntityManagerRequest

  /**
   * Trait to represent responses given by [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]]
   */
  sealed trait EntityManagerResponse

  /**
   * Trait to represent a generic join response
   */
  sealed trait JoinResponse extends EntityManagerResponse

  /**
   * Message to represent a success on join
   */
  case object JoinOK extends JoinResponse

  /**
   * Message used to return entity info.
   * @param entity it contains info of an entity
   */
  final case class EntityResult(entity: DeviceChannel) extends EntityManagerResponse

  /**
   * Message used to say that an entity doesn't exist in this entity manager
   */
  case object EntityNotFound extends EntityManagerResponse

  /**
   * Message as a success response on a remove request.
   */
  case object EntityRemoved extends EntityManagerResponse
  /**
   * Message to represent an error on join
   */
  final case class JoinError(error: String) extends JoinResponse

  /**
   * Trait to represent requests forwarded to [[it.unibo.intelliserra.server.zone.ZoneManagerActor]]
   */
  /* ZoneManagerProtocol (From GH to ZoneManager) */
  sealed trait ZoneManagerRequest
  /**
   * Message used to create a new zone with the specified name
   * @param zoneName identifier of the zone
   */
  final case class CreateZone(zoneName: String) extends ZoneManagerRequest

  /**
   * Message sent to remove a zone, if exists
   * @param zoneName identifier of the zone to remove
   */
  final case class RemoveZone(zoneName: String) extends ZoneManagerRequest

  /**
   * Message sent to ask for all the zones in [[it.unibo.intelliserra.server.zone.ZoneManagerActor]]
   */
  case object GetZones extends ZoneManagerRequest

  /**
   * Message sent to assign a registered entity to a zone, if the zone exists and the entity
   * is not already assigned
   * @param zoneName identifier of the zone to whom assign the entity
   * @param entityChannel registered entity to assign
   */
  final case class AssignEntityToZone(zoneName: String, entityChannel: DeviceChannel) extends ZoneManagerRequest

  /**
   * Message sent to dissociate a registered entity from a zone, if the entity is associated to any
   * @param entityChannel registered entity to dissociate
   */
  final case class DissociateEntityFromZone(entityChannel: DeviceChannel) extends ZoneManagerRequest

  /**
   * Message sent to ask for the state of a zone, whether it exists
   * @param zoneName identifier of the interested zone
   */
  final case class GetZoneState(zoneName: String) extends ZoneManagerRequest

  /**
   * Trait to represent responses given by [[it.unibo.intelliserra.server.zone.ZoneManagerActor]]
   */
  sealed trait ZoneManagerResponse

  /**
   * Message sent as a successful creation response
   */
  case object ZoneCreated extends ZoneManagerResponse

  /**
   * Message sent as a successful cancellation response
   */
  case object ZoneRemoved extends ZoneManagerResponse

  /**
   * Message sent as a failure creation response
   */
  case object ZoneAlreadyExists extends ZoneManagerResponse

  /**
   * Message sent to say that specified zone doesn't exist
   */
  case object ZoneNotFound extends ZoneManagerResponse

  /**
   * Message sent to tell which zones there are in [[it.unibo.intelliserra.server.zone.ZoneManagerActor]]
   * @param zones identifiers of the zones inside the [[it.unibo.intelliserra.server.zone.ZoneManagerActor]]
   */
  final case class ZonesResult(zones: List[String]) extends ZoneManagerResponse

  /**
   * Message sent as a failure response on assignment
   * @param zone identifier of the zone to which the entity is already assigned
   */
  final case class AlreadyAssigned(zone: String) extends ZoneManagerResponse

  /**
   * Message sent as a successful assignment response
   */
  case object AssignOk extends ZoneManagerResponse

  /**
   * Message sent as a successful dissociation response
   */
  case object DissociateOk extends ZoneManagerResponse

  /**
   * Message used to represent a generic error on assign request
   * @param error explanation of the error
   */
  final case class AssignError(error: String) extends ZoneManagerResponse

  /**
   * Message sent as a failure response on dissociation
   */
  case object AlreadyDissociated extends ZoneManagerResponse

  /**
   * Trait to represent requests forwarded to [[it.unibo.intelliserra.server.zone.ZoneActor]]
   */
  sealed trait ZoneRequest

  /**
   * Message sent to add the specified entity to the ones associated to [[it.unibo.intelliserra.server.zone.ZoneActor]]
   * @param entityChannel entity to add
   */
  final case class AddEntity(entityChannel: DeviceChannel) extends ZoneRequest
  /**
   * Message sent to remove the specified entity from the ones associated to [[it.unibo.intelliserra.server.zone.ZoneActor]]
   * @param entityChannel entity to add
   */
  final case class DeleteEntity(entityChannel: DeviceChannel) extends ZoneRequest

  /**
   * Message sent to get the state of the zone
   */
  case object GetState extends ZoneRequest

  /**
   * Message sent to say what are the inferred action
   * @param actions inferred actions
   */
  final case class DoActions(actions: Set[Action]) extends ZoneRequest

  /**
   * Message sent by [[it.unibo.intelliserra.server.zone.ZoneActor]] to communicate its state
   * @param state state of the zone
   */
  final case class MyState(state : State)

  /**
   * Trait to represent requests forwarded to [[it.unibo.intelliserra.server.entityManager.EntityManagerActor]]
   */
  sealed trait EntityRequest

  /**
   * Message sent to dissociate an entity from a zone
   * @param zoneRef actorRef of the zone
   * @param zoneID identifier of the zone
   */
  final case class DissociateFrom(zoneRef: ActorRef, zoneID: String) extends EntityRequest//From ZoneManager to Sensor/ Actuator
  /**
   * Message sent to associate an entity to a zone
   * @param zoneRef actorRef of the zone
   * @param zoneID identifier of the zone
   */
  final case class AssociateTo(zoneRef: ActorRef, zoneID: String) extends EntityRequest//From ZoneManager to Sensor/ Actuator

  /**
   * Message sent to inform that an [[it.unibo.intelliserra.common.communication.Messages.AssociateTo]] message is received
   */
  /* --- From Sensor/Actuator to ZoneManager --- */
  case object Ack

  /**
   * Trait to represent requests forwarded to [[it.unibo.intelliserra.server.rule.RuleEngineService]]
   */
  sealed trait RuleEntityManagerRequest

  /**
   * Message sent to enable an existing rule
   * @param ruleID rule identifier
   */
  final case class EnableRule(ruleID: String) extends RuleEntityManagerRequest

  /**
   * Message sent to disable an existing rule
   * @param ruleID rule identifier
   */
  final case class DisableRule(ruleID: String) extends RuleEntityManagerRequest

  /**
   * Message sent to infer actions from the state of a zone
   * @param state represent the state of zone
   */
  final case class InferActions(state: State) extends RuleEntityManagerRequest

  /**
   * Message sent to get all rules
   */
  case object GetRules extends  RuleEntityManagerRequest

  /**
   * Trait to represent answers obtained from [[it.unibo.intelliserra.server.rule.RuleEngineService]]
   */
  sealed trait RuleEntityResponse

  /**
   * Message obtained, containing all the rules
   * @param ruleInfo list of all rules.
   */
  final case class Rules(ruleInfo: List[RuleInfo]) extends RuleEntityResponse

  /**
   * Message sent when a rule has been successfully disabled
   */
  case object DisableOk extends RuleEntityResponse

  /**
   * Message sent when a rule has been successfully enabled
   */
  case object EnableOk extends RuleEntityResponse

  /**
   * Message sent when the rule does not exist or
   * when you are asked to enable an already enabled rule and vice versa.
   */
  case object Error extends RuleEntityResponse

  /* --- From SensorActor to ZoneActor --- */
  final case class SensorMeasureUpdated(measure: Measure)

  /* --- From ActuatorActor to ZoneActor --- */
  final case class ActuatorStateChanged(operationalState: OperationalState)
}
