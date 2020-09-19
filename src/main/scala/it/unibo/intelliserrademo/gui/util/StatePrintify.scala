package it.unibo.intelliserrademo.gui.util

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.Measure
import it.unibo.intelliserra.core.state.State

object StatePrintify {

  private[gui] def statePrintify(state: Option[State]): String = {
    state.fold("No computed state ")(full => perceptionsPrintify(full.perceptions) + " -> " + activeActionsPrintify(full.activeActions))
  }

  private def perceptionsPrintify(perceptions: List[Measure]): String = {
    perceptions.map(perception => perception.category + ": " + perception.value).mkString(",")
  }

  private def activeActionsPrintify(activeActions: List[Action]): String = {
    activeActions.mkString(", ")
  }

}
