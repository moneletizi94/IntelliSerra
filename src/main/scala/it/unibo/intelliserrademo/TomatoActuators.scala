package it.unibo.intelliserrademo

import java.util.TimerTask

import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState, TimedAction, TimedTask}
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.ActingCapability
import it.unibo.intelliserrademo.CategoriesAndActions.{Dehumidifies, Fan, Heat, Water}
import it.unibo.intelliserrademo.customsensor.SimulatedDevice.CustomActuator

import scala.concurrent.duration._
object TomatoActuators {

  object WaterActuator extends FakeTimedActuator[Water]
  object HeatActuator extends FakeTimedActuator[Heat]
  object FanActuator extends FakeTimedActuator[Fan]

  class FakeTimedActuator[A <: TimedAction](){
    /*def apply(name : String): CustomActuator = CustomActuator(name, Set(Fan), {
      case (_, action: A) =>
        val actionName = action.getClass.getSimpleName
        printDoingTimedAction(actionName, action.time) ; TimedTask(action.time, () => println(s"$actionName finished"))
    })*/
  }

  object Dehumidifiers {
    /*def apply(name : String): CustomActuator = CustomActuator(name, Set(Dehumidifies), {
      case (_, Dehumidifies(true)) => println("switched off") ; TimedTask.now()
      case (_, Dehumidifies(false)) => println("switched on"); TimedTask.now()
    })*/
  }

  private def printDoingTimedAction(actionName : String, duration: FiniteDuration): Unit = println(s"start doing $actionName for $duration seconds")

}
