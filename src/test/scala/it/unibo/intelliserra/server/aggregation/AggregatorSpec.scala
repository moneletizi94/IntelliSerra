package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, StringType}
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation.Aggregator.createAggregator
import it.unibo.intelliserra.utils.Categories.{Temperature, Weather}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import it.unibo.intelliserra.utils.TestUtility
import org.scalatestplus.junit.JUnitRunner

import scala.util.Success

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class AggregatorSpec extends FlatSpec with Matchers with TestUtility {

  private val intTempMeasures : List[Measure] = List(Measure(Temperature)(4), Measure(Temperature)(5), Measure(Temperature)(8))
  private val stringWeatherMeasures : List[Measure] = List(Measure(Weather)("RAINY"), Measure(Weather)("SUNNY"), Measure(Weather)("SUNNY"))

  "An aggregator " should "aggregate measures correctly using avg function" in {
    val aggregatedMeasure = createAggregator(Temperature)(avg).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(Temperature)(5))
  }

  "An aggregator " should "aggregate measures correctly using sum function" in {
    val aggregatedMeasure = createAggregator(Temperature)(sum).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(Temperature)(17))
  }

  "An aggregator " should "aggregate measures correctly using min function" in {
    val aggregatedMeasure = createAggregator(Temperature)(min).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(Temperature)(4))
  }

  "An aggregator " should "aggregate measures correctly using max function" in {
    val aggregatedMeasure = createAggregator(Temperature)(max).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(Measure(Temperature)(8))
  }

  "An aggregator of textual type" should "aggregate measures correctly" in {
    val aggregatedMeasure = createAggregator(Weather)(moreFrequent).aggregate(stringWeatherMeasures)
    aggregatedMeasure shouldBe Success(Measure(Weather)("SUNNY"))
  }

}
