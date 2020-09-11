package it.unibo.intelliserra.examples

import alice.tuprolog.{Struct, Term}

object PrologExample extends App {

  import alice.tuprolog.Prolog

  val engine = new Prolog

  val biboTerm = Term.createTerm("measure(20, humidity).")
  val biboTerm2 = Term.createTerm("measure(10, temperature).")

  val ss = Struct.list(biboTerm, biboTerm2)

  println(engine.solve("measure(10, Y).").getSolution)
}
