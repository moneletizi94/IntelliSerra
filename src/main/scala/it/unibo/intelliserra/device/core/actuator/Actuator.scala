package it.unibo.intelliserra.device.core.actuator

import it.unibo.intelliserra.core.action.{Action, DoingActions, OperationalState}
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, ActionTag}
import it.unibo.intelliserra.core.entity.{Capability, Device}
import it.unibo.intelliserra.device.core.actuator.Actuator.ActionHandler


trait Actuator extends Device {
  private var _state: OperationalState = OperationalState()
  private var pendingOperation = Map[Action, Operation]()

  def state: OperationalState = _state

  def handleAction(action: Action): Option[Operation] = {
    val operation = Option(action)
      .filter(action => capability.includes(Capability.acting(action.getClass)) && !state.isDoing(action))
      .flatMap(action => actionHandler.lift((state, action)))

    operation match {
      case Some(value) => pendingOperation += action -> value; _state += action; Option(value)
      case None => None
    }
  }

  def handleCompletedAction(action: Action): OperationalState = state match {
    case DoingActions(actions) if actions contains action => _state = completeAction(action); _state
    case _ => _state
  }

  private def completeAction(action: Action): OperationalState = {
    pendingOperation.get(action).foreach(_.complete)
    pendingOperation -= action
    state - action
  }

  /**
   * Action handler define by
   * @return
   */
  protected def actionHandler: ActionHandler
}

object Actuator {
  type ActionHandler = PartialFunction[(OperationalState, Action), Operation]

  private class DefaultActuatorImpl(override val identifier: String,
                                    override val capability: ActingCapability,
                                    override val actionHandler: ActionHandler) extends Actuator

  def apply(identifier: String,
            supportedActions: Set[ActionTag],
            handler: ActionHandler): Actuator = apply(identifier, Capability.acting(supportedActions))(handler)

  def apply(identifier: String,
            capability: ActingCapability)
           (handler: ActionHandler): Actuator =
    new DefaultActuatorImpl(identifier, capability, handler)
}
