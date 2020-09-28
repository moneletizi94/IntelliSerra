package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.perception.{Category, DoubleType, IntType, Measure, NumericType, ValueType}

import scala.math.Fractional
import scala.util.Try

/**
 * It represents the concept of measure aggregator.
 * Each aggregator refers to a specific category and defines how measures of the same category should be aggregated.
 */
trait Aggregator {
  /**
   * the category to which the aggregator refers
   * @return
   */
  def category : Category[ValueType]

  /**
   *
   * @param measures Measures to be aggregate
   * @return Failure if if at least one measure does not contain a value consistent with the category to which it belongs; Success containing the aggregated measure otherwise
   */
  def aggregate(measures : List[Measure]) : Try[Measure]
}

object Aggregator{

  implicit def ImplicitNumericIntTypeToOps(int: IntType): intTypeFractional.FractionalOps = intTypeFractional.mkNumericOps(int)
  implicit def ImplicitNumericDoubleTypeToOps(double: DoubleType): doubleTypeFractional.FractionalOps = doubleTypeFractional.mkNumericOps(double)

  /**
   * @param category The aggregator category
   * @param aggregateFunction The aggregation strategy
   * @tparam V The type of the values of the measures to be aggregate
   * @return an aggregator of the specified category that uses the defined aggregation strategy to aggreagte measures
   */
  def createAggregator[V <: ValueType](category: Category[V])(aggregateFunction : List[V] => V) : Aggregator =
    new BaseAggregator(category)(aggregateFunction)

  private class BaseAggregator[V <: ValueType](override val category: Category[V])(val f : List[V] => V) extends Aggregator {
    override def aggregate(measures: List[Measure]): Try[Measure] = Try{ Measure(category)(f(measures.map(_.value.asInstanceOf[V]))) }
  }

}

/**
 * This object provides some common aggregation functions useful to work with a list of ValueType
 */
object AggregationFunctions{
  def avg[A <: NumericType : Fractional] : List[A] => A = list => list.avg(implicitly[Fractional[A]])
  def sum[A <: NumericType : Fractional] : List[A] => A = list => list.sum(implicitly[Fractional[A]])
  def min[A <: NumericType : Ordering] : List[A] => A = list => list.min(implicitly[Ordering[A]])
  def max[A <: NumericType : Ordering]: List[A] => A = list => list.max(implicitly[Ordering[A]])
  def moreFrequent[A <: ValueType] : List[A] => A = list => list.computeFrequency.maxBy(_._2)._1
  def lessFrequent[A <: ValueType] : List[A] => A = list => list.computeFrequency.minBy(_._2)._1
  implicit val ImplicitNumericIntType: Fractional[IntType] = intTypeFractional
  implicit val ImplicitNumericDoubleType: Fractional[DoubleType] = doubleTypeFractional

}