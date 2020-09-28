package it.unibo.intelliserra.device.core.actuator

import it.unibo.intelliserra.core.action.{Action, DoingActions, OperationalState}
import it.unibo.intelliserra.core.entity.Capability.{ActingCapability, ActionTag}
import it.unibo.intelliserra.core.entity.{Capability, Device}
import it.unibo.intelliserra.device.core.actuator.Actuator.ActionHandler

/**
 * Rich interface that represent an actuator.
 * It allow to define an action handler in order to map an [[Action]] to concrete [[Operation]].
 */
trait Actuator extends Device {

  /**
   * The operational state of actuator
   */
  private var _state: OperationalState = OperationalState()

  /**
   * Pending operation of actuator. See [[Operation]]
   */
  private var pendingOperation = Map[Action, Operation]()

  /** Expose the actual state of actuator */
  def state: OperationalState = _state

  /**
   * Activate the action on the actuator if the capability includes the ability to do the action.
   * It cause a state change.
   * @param action action to be handled
   * @return the operation to be executed defined by ActionHandler
   */
  def handleAction(action: Action): Option[Operation] = {
    val operation = Option(action)
      .filter(action => capability.includes(Capability.acting(action.getClass)) && !state.isDoing(action))
      .flatMap(action => actionHandler.lift((state, action)))

    operation match {
      case Some(value) => pendingOperation += action -> value; _state += action; Option(value)
      case None => None
    }
  }

  /**
   * Terminate an action on the actuator if the action is active.
   * Its cause a state change.
   * @param action action to be terminated
   * @return  the new state of actuator
   */
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
   * Function for define what operation execute when an action is activated.
   * This mapping function is defined by concrete realization.
   * @return the ActionHandler. [[ActionHandler]]
   */
  protected def actionHandler: ActionHandler
}

object Actuator {
  /**
   * Partial function for define what operation to do starting from actual state and an active action.
   */
  type ActionHandler = PartialFunction[(OperationalState, Action), Operation]


  /**
   * Factory for creation of simple actuator specifying the action handler.
   * @param identifier        the name of actuator
   * @param supportedActions  the action supported by actuators
   * @param handler           the action handler
   * @return the actuator that use the specified action handler
   */
  def apply(identifier: String,
            supportedActions: Set[ActionTag],
            handler: ActionHandler): Actuator = apply(identifier, Capability.acting(supportedActions))(handler)

  /**
   * Factory for creation of simple actuator specifying the action handler.
   * @param identifier  the name of actuator
   * @param capability  the acting capability of actuator
   * @param handler     the action handler
   * @return the actuator that use the specified action handler
   */
  def apply(identifier: String,
            capability: ActingCapability)
           (handler: ActionHandler): Actuator =
    new DefaultActuatorImpl(identifier, capability, handler)


  private class DefaultActuatorImpl(override val identifier: String,
                                    override val capability: ActingCapability,
                                    override val actionHandler: ActionHandler) extends Actuator
}
