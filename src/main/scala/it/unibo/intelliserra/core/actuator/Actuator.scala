package it.unibo.intelliserra.core.actuator

import java.util.concurrent.Executors

import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.ActingCapability
import monix.reactive.Observable

import scala.concurrent.{ExecutionContext, Future}

trait Actuator {
  def identifier: String
  def capability: ActingCapability
  def state: Observable[OperationalState]
  def actionHandler: ActionHandler
}

object Actuator {
  type ActionHandler = PartialFunction[Action, Future[OperationalState]]

  /*abstract class BasicActuator(override val identifier: String,
                               override val capability: ActingCapability) extends Actuator {

    private var currentState: OperationalState = Idle

    // protected def

  }

  private class BasicActuator(override val identifier: String,
                              override val capability: ActingCapability,
                              private val actionHandler: ActionHandler,
                              private val initialState: OperationalState = Idle) extends Actuator {

    private implicit val executionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    override def state: OperationalState = initialState
    override def doAction(action: Action): Future[Actuator] = actionHandler(action).map {
      newState => new BasicActuator(identifier, capability, actionHandler, newState)
    }
  }*/
}