package it.unibo.intelliserra.core

import alice.tuprolog.{Term}

package object prolog {

  implicit class RichAny[A: PrologRepresentation](any: A){
    def toTerm: Term = implicitly[PrologRepresentation[A]].toTerm(any)
  }
}
