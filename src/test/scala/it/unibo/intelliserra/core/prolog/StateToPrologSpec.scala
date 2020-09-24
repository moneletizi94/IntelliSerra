package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.perception
import it.unibo.intelliserra.core.perception.Measure
import it.unibo.intelliserra.core.prolog.Representations._
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.utils.TestUtility.Categories._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StateToPrologSpec extends WordSpecLike with Matchers with BeforeAndAfter {

  private var measures: List[(Measure, String)] = _

  before {
    measures = List[(Measure, String)](
      perception.Measure(Temperature)(10) -> "measure(10, temperature).",
      perception.Measure(Weather)("sun") -> "measure(string(sun), weather).",
      perception.Measure(LightToggle)(true) -> "measure(1, lighttoggle).",
      perception.Measure(Pressure)(1056.2) -> "measure(1056.2, pressure).",
      perception.Measure(CharCategory)('a')-> s"measure(${'a'.toInt}, charcategory)."
    )
  }

  "A prolog state converter " must {
    "convert state to right prolog struct" in {

      val expectedStruct = measures.map(_._2).map(Term.createTerm)
      val state = State(measures.map(_._1), List())

      state.toTerm shouldBe Struct.list(expectedStruct:_*)
    }
  }
}
