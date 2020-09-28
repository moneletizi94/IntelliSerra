package it.unibo.intelliserrademo.customdevice

import it.unibo.intelliserra.core.action.TimedAction
import it.unibo.intelliserrademo.common.CategoriesAndActions.{Dehumidifies, Fan, Heat, Water}

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag
import it.unibo.intelliserra.device.core.actuator.{Actuator, Operation}

object TomatoActuators {

  object WaterActuator extends FakeTimedActuator[Water]
  object HeatActuator extends FakeTimedActuator[Heat]
  object FanActuator extends FakeTimedActuator[Fan]

  class FakeTimedActuator[A <: TimedAction : ClassTag]{
    def apply(name : String): Actuator = Actuator(name, Set(implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]]), {
      case (_, action: A) =>
        val actionName = action.getClass.getSimpleName
        printDoingTimedAction(actionName, action.time); Operation.completeAfter(action.time, () => println(s"$actionName finished"))
    })
  }

  object Dehumidifiers {
    def apply(name : String): Actuator = Actuator(name, Set(classOf[Dehumidifies]), {
      case (_, Dehumidifies(true)) => println("switched off"); Operation.completed()
      case (_, Dehumidifies(false)) => println("switched on"); Operation.completed()
    })
  }

  private def printDoingTimedAction(actionName : String, duration: FiniteDuration): Unit = println(s"start doing $actionName for $duration seconds")

}
