package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule

trait PrologRepresentation[T] {
  def toTerm(data: T): Struct
}




