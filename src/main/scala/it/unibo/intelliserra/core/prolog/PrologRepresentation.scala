package it.unibo.intelliserra.core.prolog

import alice.tuprolog.Struct

trait PrologRepresentation[T] {
  def toTerm(data: T): Struct
}