package it.unibo.intelliserra.core

import alice.tuprolog.Term
import it.unibo.intelliserra.core.prolog.Representations._

package object prolog {
  import it.unibo.intelliserra.core.prolog.Representations._
  implicit class RichAny[A : PrologRepresentation](toEnrich : A) {
    def toTerm : Term = {
      implicitly[PrologRepresentation[A]].toTerm(toEnrich)
    }
  }
}
