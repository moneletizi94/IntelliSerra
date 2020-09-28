package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.perception
import it.unibo.intelliserra.core.perception.{DoubleType, IntType, Measure, StringType}
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation.Aggregator._
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import it.unibo.intelliserra.utils.TestUtility
import it.unibo.intelliserra.utils.TestUtility.Categories._
import it.unibo.intelliserrademo.common.CategoriesAndActions.AirTemperature
import org.scalatestplus.junit.JUnitRunner

import scala.util.Success

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class AggregatorSpec extends FlatSpec with Matchers with TestUtility {

  private val intTempMeasures : List[Measure] = List(perception.Measure(Temperature)(4), perception.Measure(Temperature)(5), perception.Measure(Temperature)(8))
  private val stringWeatherMeasures : List[Measure] = List(perception.Measure(Weather)("RAINY"), perception.Measure(Weather)("SUNNY"), perception.Measure(Weather)("SUNNY"))

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

  "An aggregator of textual type" should "aggregate measures correctly using lessFrequent function" in {
    val aggregatedMeasure = createAggregator(Weather)(moreFrequent).aggregate(stringWeatherMeasures)
    aggregatedMeasure shouldBe Success(Measure(Weather)("SUNNY"))
  }

  "An aggregator of textual type" should "aggregate measures correctly using moreFrequent function" in {
    val aggregatedMeasure = createAggregator(Weather)(lessFrequent).aggregate(stringWeatherMeasures)
    aggregatedMeasure shouldBe Success(Measure(Weather)("RAINY"))
  }

  "An aggregator of textual type" should "aggregate measures correctly using custom aggregate function" in {
    val aggregatedMeasure =  createAggregator(Temperature)(list => list.foldLeft(IntType(0))((a1, a2) => if(a2.value > 4) a1+a2 else a1)).aggregate(intTempMeasures)
    aggregatedMeasure shouldBe Success(perception.Measure(Temperature)(13))
  }

}
