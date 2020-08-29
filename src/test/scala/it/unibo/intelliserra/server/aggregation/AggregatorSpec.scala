package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.sensor.{Category, DoubleType, IntType, Measure}
import org.scalatest.{FlatSpec, Matchers}
import it.unibo.intelliserra.server.aggregation.Aggregator._
import it.unibo.intelliserra.server.aggregation._

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class AggregatorSpec extends FlatSpec with Matchers{

  private object Temperature extends Category{
    override type Value = IntType
  }

  // TODO: create default category; in every test class we repeat the object creation

  "An aggregator " should "aggregate measures correctly " in {
    val measures = List(Measure(4, Temperature), Measure(5, Temperature), Measure(8, Temperature))
    val aggregatedMeasure = createAggregator(Temperature)(_.avg).aggregate(measures)
    aggregatedMeasure shouldBe Success(Measure(5,Temperature))
  }

  "An aggregator " should "aggregate measures correctly 2" in {
    val measures = List(Measure(4, Temperature), Measure(5, Temperature), Measure(8, Temperature))
    //val aggregatedMeasure = createAggregator(Temperature)(AggregateFunctions.sum).aggregate(measures)
    //aggregatedMeasure shouldBe Success(Measure(17,Temperature))
  }

  "An aggregator " should "not permit aggregate measures of different types " in {
    val measures = List(Measure(4, Temperature), Measure('c', Temperature), Measure(8, Temperature))
    //createAggregator(Temperature).aggregate(measures).isFailure
  }

}
