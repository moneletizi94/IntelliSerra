package it.unibo.intelliserrademo

import java.util.TimerTask

import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState, TimedTask}
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.ActingCapability
import it.unibo.intelliserrademo.CategoriesAndActions.{Fan, Heat, Water}
import it.unibo.intelliserrademo.customsensor.SimulatedDevice.CustomActuator

import scala.concurrent.duration._
object TomatoActuators {

  object WaterActuator {
    def apply(name : String): CustomActuator = CustomActuator(name, Set(Water), {
     case (_, Water(time)) => ; TimedTask(Fan, time)(_ => println("water finished"))
    })
  }

  object HeatActuator {
    def apply(name : String): CustomActuator = CustomActuator(name, Set(Heat), {
      case (_, Water(time)) => ; TimedTask(Fan, time)(_ => println("water finished"))
    })
  }

  object FanActuator {
    def apply(name : String): CustomActuator = CustomActuator(name, Set(Fan), {
      case (_, Water(time)) => ; TimedTask(Fan, time)(_ => println("water finished"))
    })
  }

  private def printDoingAction(actionName : String, duration: FiniteDuration) = println(s"start doing $actionName for $duration seconds")



}
