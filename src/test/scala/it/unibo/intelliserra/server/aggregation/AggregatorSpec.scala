package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, StringType}
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation.Aggregator.createAggregator
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import it.unibo.intelliserra.utils.TestUtility
import org.scalatestplus.junit.JUnitRunner

import scala.util.Success

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class AggregatorSpec extends FlatSpec with Matchers with TestUtility {

  private val intTempMeasures : List[Measure] = List(Measure(4, Temperature), Measure(5, Temperature), Measure(8, Temperature))
  private val stringWeatherMeasures : List[Measure] = List(Measure("RAINY", Weather), Measure("SUNNY", Weather), Measure("SUNNY", Weather))

  "An aggregator " should "aggregate measures correctly using avg function" in {
    val aggregatedMeasure = createAggregator(Temperature)(avg).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(5,Temperature))
  }

  "An aggregator " should "aggregate measures correctly using sum function" in {
    val aggregatedMeasure = createAggregator(Temperature)(sum).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(17,Temperature))
  }

  "An aggregator " should "aggregate measures correctly using min function" in {
    val aggregatedMeasure = createAggregator(Temperature)(min).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(4,Temperature))
  }

  "An aggregator " should "aggregate measures correctly using max function" in {
    val aggregatedMeasure = createAggregator(Temperature)(max).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(8,Temperature))
  }

  "An aggregator " should "not permit aggregate measures of different types " in {
    createAggregator(Temperature)(_.avg).aggregate(intTempMeasures.+:(Measure('c',Temperature))).isFailure
  }

  "An aggregator of textual type" should "aggregate measures correctly" in {
    val aggregatedMeasure = createAggregator(Weather)(moreFrequent).aggregate(stringWeatherMeasures)
    aggregatedMeasure shouldBe Success(Measure("SUNNY",Weather))
  }

}
