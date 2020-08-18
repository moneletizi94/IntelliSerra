package it.unibo.intelliserra.core.actuator

sealed trait Action
final case class DoingAction(action : Action)
