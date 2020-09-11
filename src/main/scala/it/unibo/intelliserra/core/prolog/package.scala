package it.unibo.intelliserra.core

import alice.tuprolog.Struct
import it.unibo.intelliserra.core.prolog.Representations.{ActionPrologRepresentation, ConditionStatementPrologRepresentation, RulePrologRepresentation}

package object prolog {
  implicit class RichAny[A : PrologRepresentation](toEnrich : A) {
    def toTerm : Struct = {
      implicitly[PrologRepresentation[A]].toTerm(toEnrich)
    }
  }

  implicit val prologRepresentationRule = RulePrologRepresentation
  implicit val prologRepresentationAction = ActionPrologRepresentation
  implicit val prologRepresentationConditionStatement = ConditionStatementPrologRepresentation
}
