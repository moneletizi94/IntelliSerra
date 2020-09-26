package it.unibo.intelliserra.common.communication

/**
 * Object to describe all the messages exchanged between client and server with
 * a request-response fashion.
 */
object Protocol {

  /**
   * Trait to represent all the requests delivered to [[it.unibo.intelliserra.client.core.GreenHouseClient]]
   */
  sealed trait ClientRequest

  /**
   * Message sent to create a new zone given a name.
   * @param zoneName specified name which will be the zone identifier
   */
  final case class CreateZone(zoneName: String) extends ClientRequest

  /**
   * Message sent to delete a zone given a name.
   * @param zoneName name of the zone to remove
   */
  final case class DeleteZone(zoneName: String) extends ClientRequest

  /**
   * Message sent to know all the existent zones
   */
  final case class GetZones() extends ClientRequest

  /**
   * Message sent to assign an entity (both actuator and sensor) to a zone
   * @param zoneName zone to whom assign the entity
   * @param entityId identifier of the entity to assign
   */
  final case class AssignEntity(zoneName: String, entityId: String) extends ClientRequest

  /**
   * Message sent to dissociate an entity (both actuator and sensor) from a zone, whether
   * it is assigned to some zones
   * @param entityId identifier of the dissociating entity
   */
  final case class DissociateEntity(entityId: String) extends ClientRequest

  /**
   * Message sent to remove an entity (both actuator and sensor) from the system
   * @param entityId identifier of the removing entity
   */
  final case class RemoveEntity(entityId: String) extends ClientRequest

  /**
   * Message sent to discover the state of a zone and its sensors/actuators
   * @param zoneName name of the zone
   */
  final case class GetState(zoneName: String) extends ClientRequest

  /**
   * Message sent to enable an existing rule
   * @param ruleID rule identifier
   */
  final case class EnableRule(ruleID: String) extends ClientRequest

  /**
   * Message sent to disable an existing rule
   * @param ruleID rule identifier
   */
  final case class DisableRule(ruleID: String) extends ClientRequest

  /**
   * Message sent to obtain all rules
   */
  final case object GetRules extends ClientRequest

  /**
   * Trait to represent all the responses delivered to the client
   */
  sealed trait ResponseType

  /**
   * Message sent to signal a generic success after a request
   */
  case object Ok extends ResponseType

  /**
   * Message sent to signal a creation success
   */
  case object Created extends ResponseType
  /**
   * Message sent to signal a cancellation success
   */
  case object Deleted extends ResponseType

  /**
   * Message to signal a generic error
   */
  case object Error extends ResponseType

  /**
   * Message sent to signal a not found error
   */
  case object NotFound extends ResponseType

  /**
   * Message sent to signal a conflict error
   */
  case object Conflict extends ResponseType

  /**
   * Message used to wrap a response, indicating an optional payload which explain the response
   * @param responseType the type of the response
   * @param payload it explains the response
   */
  final case class ServiceResponse(responseType: ResponseType, payload: java.io.Serializable = "")
}
