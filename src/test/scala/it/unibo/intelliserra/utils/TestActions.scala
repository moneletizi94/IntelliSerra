package it.unibo.intelliserra.utils

import it.unibo.intelliserra.core.actuator.Action

trait TestActions {
  case object Water extends Action
  case object OpenWindow extends Action
}
