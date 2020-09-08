package it.unibo.intelliserra.common.communication

import it.unibo.intelliserra.core.state.State

/**
 *  Decision
 *
 *  A ? GetState(zone) => Future[ZoneManagerResponse] flatMap {
 *      case s: Success => Future.success(s)
 *      case f: Fail => Future.fail(new Exception(f))
 *  }
 *
 *  A:
 *    case CreateZone(zone) => .... sender ! AlreadyExist
 *
 */
object Protocol {

  // Client Protocol
  sealed trait ClientRequest
  final case class CreateZone(zoneName: String) extends ClientRequest
  final case class DeleteZone(zoneName: String) extends ClientRequest
  final case class GetZones() extends ClientRequest
  final case class AssignEntity(zoneName: String, entityId: String) extends ClientRequest
  final case class DissociateEntity(entityId: String) extends ClientRequest
  final case class RemoveEntity(entityId: String) extends ClientRequest
  final case class GetState(zoneName: String) extends ClientRequest
  final case class EnableRule(ruleID: String) extends ClientRequest
  final case class DisableRule(ruleID: String) extends ClientRequest
  final case object GetRules extends ClientRequest

  sealed trait ResponseType
  case object Ok extends ResponseType
  case object Created extends ResponseType
  case object Deleted extends ResponseType
  case object NotFound extends ResponseType
  case object Conflict extends ResponseType
  case object Error extends ResponseType

  final case class ServiceResponse(responseType: ResponseType, payload: java.io.Serializable = "")
}
