package it.unibo.intelliserra.core.prolog

import alice.tuprolog.Term

trait PrologRepresentation[T] {
  def toTerm(data: T): Term
}

