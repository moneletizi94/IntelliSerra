package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.examples.RuleDslExample.{Fan, Humidity, Temperature, Water}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import it.unibo.intelliserra.core.rule.dsl._
import it.unibo.intelliserra.core.prolog.Representations._

@RunWith(classOf[JUnitRunner])
class RuleToPrologSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter {

  private var rules: Map[Rule, List[String]]= Map()

  before {
    rules += ((Temperature > 10 execute Water) -> List("action(water):-measure(X0, temperature), X0 > 10."),
      (Temperature > 20 && Humidity > 50.0 execute Fan) -> List("action(fan):-(measure(X0, temperature), X0>20), measure(X1, humidity), X1 > 50.0."),
        (Humidity > 10.0 executeMany Set(Water, Fan)) -> List("action(water):-measure(X0, humidity), X0 > 10.0.",
          "action(fan):-measure(X0, humidity), X0> 10.0."))
  }

  "A rule prolog converter" must {
    "convert a rule to the rights prolog terms" in {
      val testingRuleTerms = rules.keySet.map(rule => rule.toTerm).toList
      val expectedTerms = rules.values.map(stringList => Struct.list(stringList.map(Term.createTerm):_*)).toList

      testingRuleTerms shouldBe expectedTerms
    }
  }
}
