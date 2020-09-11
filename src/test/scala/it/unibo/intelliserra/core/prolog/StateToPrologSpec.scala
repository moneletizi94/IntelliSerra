package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.sensor.{Category, Measure, ValueType}
import it.unibo.intelliserra.core.state.State
import it.unibo.intelliserra.utils.TestUtility.Categories.{CharCategory, LightToggle, Pressure, Temperature, Weather}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StateToPrologSpec extends WordSpecLike with Matchers with BeforeAndAfter {

  private var measures: List[Measure] = _

  before {
    measures = measuresFromList(List(
      Temperature -> 10,
      Weather -> "sun",
      LightToggle -> true,
      Pressure -> 1056.2,
      CharCategory -> 'a'
    ))
  }

  "A prolog state converter " must {
    "convert state to right prolog struct" in {

      val expectedStruct = List(
        "measure(10, temperature).",
        "measure(string(sun), weather).",
        "measure(1, lighttoggle).",
        "measure(1056.2, pressure).",
        s"measure(${'a'.toInt}, charcategory)."
      )

      val state = State(measures, List())
      val stateToProlog = new StateToProlog()

      stateToProlog.toTerm(state) shouldBe Struct.list(expectedStruct.map(Term.createTerm):_*)
    }

  }

  private def measuresFromList(measureMap: List[(Category[ValueType], ValueType)]): List[Measure] =
    measureMap.map(kv => Measure(kv._1)(kv._2))
}
