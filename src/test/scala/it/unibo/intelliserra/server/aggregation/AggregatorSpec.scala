package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure}
import org.scalatest.{FlatSpec, Matchers}
import it.unibo.intelliserra.server.aggregation.Aggregator._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AggregatorSpec extends FlatSpec with Matchers{

  private object Temperature extends Category
  // TODO: create default category; in every test class we repeat the object creation

  "An aggregator " should "aggregate measures correctly " in {
    val measures = List(Measure(IntType(4), Temperature), Measure(IntType(5), Temperature), Measure(IntType(8), Temperature))
    implicit val intSum: List[IntType] => IntType = {
      values => values.foldRight(IntType(0))((v1, v2) => IntType(v1.value + v2.value))
    }
    val aggregatedMeasure = createAggregator[IntType, IntType](Temperature).aggregate(measures)
    aggregatedMeasure shouldBe Measure(IntType(17),Temperature)
  }


}
