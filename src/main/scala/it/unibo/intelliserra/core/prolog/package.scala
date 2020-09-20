package it.unibo.intelliserra.core

import alice.tuprolog.{Prolog, Struct, Term, Theory}
import scala.collection.JavaConverters._

package object prolog {
  implicit class RichAny[A : PrologRepresentation](toEnrich : A) {
    def toTerm : Term = {
      implicitly[PrologRepresentation[A]].toTerm(toEnrich)
    }
  }

  implicit class RichProlog(engine: Prolog){

     def assertTermClauses(structClause: Struct) : Boolean = {
       Theory.fromPrologList(structClause).getClauses.asScala
         .foreach(rule => engine.solve(s"assert($rule)"))
       true
     }

    def retractTermClauses(structClause: Struct) : Boolean = {
      Theory.fromPrologList(structClause).getClauses.asScala
        .foreach(rule => engine.solve(s"retract($rule)"))
      true
    }
  }
}
