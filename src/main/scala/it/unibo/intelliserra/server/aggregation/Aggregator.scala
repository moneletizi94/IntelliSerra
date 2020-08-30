package it.unibo.intelliserra.server.aggregation
import it.unibo.intelliserra.core.sensor.{Category, Measure, NumericType, ValueType}
import scala.util.Try

trait Aggregator {
  def category : Category
  def aggregate(measures : List[Measure]) : Try[Measure]
}

object Aggregator{
  def createAggregator(category: Category)(implicit aggregateFunction : List[category.Value] => category.Value) : Aggregator =
    new BaseAggregator(category)(aggregateFunction)

  class BaseAggregator[T <: ValueType](override val category: Category)(val f : List[T] => T) extends Aggregator {
    override def aggregate(measures: List[Measure]): Try[Measure] = Try{ Measure(f(measures.map(_.value.asInstanceOf[T])), category)}
  }

  def atMostOneCategory(aggregators: List[Aggregator]) : Boolean = aggregators.groupBy(a => a.category).forall(_._2.lengthCompare(1) == 0)

}

object AggregateFunctions{
  import numericInt._
  def avg[A <: NumericType](implicit fractional : Fractional[A]) : List[A] => A = list => list.avg(fractional)
  def sum[A <: NumericType](implicit fractional : Fractional[A]) : List[A] => A = list => list.sum(fractional)
  def min[A <: NumericType](implicit ordering: Ordering[A]) : List[A] => A = list => list.min(ordering)
  def max[A <: NumericType](implicit ordering: Ordering[A]) : List[A] => A = list => list.max(ordering)
  def moreFrequent[A <: ValueType] : List[A] => A = list => list.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
}
