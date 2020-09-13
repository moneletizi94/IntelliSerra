package it.unibo.intelliserra.server.aggregation
import it.unibo.intelliserra.core.sensor.{Category, Measure, NumericType, ValueType}

import scala.util.Try

trait Aggregator {
  def category : Category[ValueType]
  def aggregate(measures : List[Measure]) : Try[Measure]
}

object Aggregator{
  def createAggregator[V <: ValueType](category: Category[V])(implicit aggregateFunction : List[V] => V) : Aggregator =
    new BaseAggregator(category)(aggregateFunction)

  class BaseAggregator[V <: ValueType](override val category: Category[V])(val f : List[V] => V) extends Aggregator {
    override def aggregate(measures: List[Measure]): Try[Measure] = Try{ Measure(category)(f(measures.map(_.value.asInstanceOf[V]))) }
  }
}

object AggregateFunctions{
  def avg[A <: NumericType](implicit fractional : Fractional[A]) : List[A] => A = list => list.avg(fractional)
  def sum[A <: NumericType](implicit fractional : Fractional[A]) : List[A] => A = list => list.sum(fractional)
  def min[A <: NumericType](implicit ordering: Ordering[A]) : List[A] => A = list => list.min(ordering)
  def max[A <: NumericType](implicit ordering: Ordering[A]) : List[A] => A = list => list.max(ordering)
  def moreFrequent[A <: ValueType] : List[A] => A = list => list.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
}
