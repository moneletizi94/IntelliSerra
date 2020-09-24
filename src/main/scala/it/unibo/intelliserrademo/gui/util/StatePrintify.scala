package it.unibo.intelliserrademo.gui.util

import it.unibo.intelliserra.core.action.Action
import it.unibo.intelliserra.core.perception.Measure
import it.unibo.intelliserra.core.state.State

object StatePrintify {

  private[gui] def statePrintify(state: State): String = {
    perceptionsPrintify(state.perceptions) +
      " -> " +
      activeActionsPrintify(state.activeActions) +
    "\n __________________________"
  }

  private def perceptionsPrintify(perceptions: List[Measure]): String = {
    "(" + perceptions.map(perception => perception.category + ": " + perception.value).mkString(",") + ")"
  }

  private def activeActionsPrintify(activeActions: List[Action]): String = {
    "(" + activeActions.mkString(",") + ")"
  }

}
