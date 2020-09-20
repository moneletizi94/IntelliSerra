package it.unibo.intelliserra.core

import alice.tuprolog.{Prolog, Struct, Term, Theory}
import it.unibo.intelliserra.core.rule.Rule

import scala.collection.JavaConverters._

package object prolog {
  implicit class RichAny[A : PrologRepresentation](toEnrich : A) {
    def toTerm : Term = {
      implicitly[PrologRepresentation[A]].toTerm(toEnrich)
    }
  }

  implicit class RichProlog(engine: Prolog){

     def assert(ruleClause: Term) : Boolean = {
       Theory.fromPrologList(ruleClause.castTo(classOf[Struct])).getClauses.asScala
         .foreach(rule => engine.solve(s"assert($rule)"))
       true
     }

    def retract(ruleClause: Term) : Boolean = {
      Theory.fromPrologList(ruleClause.castTo(classOf[Struct])).getClauses.asScala
        .foreach(rule => engine.solve(s"retract($rule)"))
      true
    }
  }
}
