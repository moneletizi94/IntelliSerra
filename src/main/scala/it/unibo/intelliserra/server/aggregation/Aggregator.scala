package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.perception.{Category, DoubleType, IntType, Measure, NumericType, ValueType}

import scala.util.Try

trait Aggregator {
  def category : Category[ValueType]
  def aggregate(measures : List[Measure]) : Try[Measure]
}

object Aggregator{
  def createAggregator[V <: ValueType](category: Category[V])(aggregateFunction : List[V] => V) : Aggregator =
    new BaseAggregator(category)(aggregateFunction)

  private class BaseAggregator[V <: ValueType](override val category: Category[V])(val f : List[V] => V) extends Aggregator {
    override def aggregate(measures: List[Measure]): Try[Measure] = Try{ Measure(category)(f(measures.map(_.value.asInstanceOf[V]))) }
  }
}

object AggregateFunctions{
  def avg[A <: NumericType : Fractional] : List[A] => A = list => list.avg(implicitly[Fractional[A]])
  def sum[A <: NumericType : Fractional] : List[A] => A = list => list.sum(implicitly[Fractional[A]])
  def min[A <: NumericType : Ordering] : List[A] => A = list => list.min(implicitly[Ordering[A]])
  def max[A <: NumericType : Ordering]: List[A] => A = list => list.max(implicitly[Ordering[A]])
  def moreFrequent[A <: ValueType] : List[A] => A = list => list.computeFrequency.maxBy(_._2)._1
  def lessFrequent[A <: ValueType] : List[A] => A = list => list.computeFrequency.minBy(_._2)._1

  implicit val ImplicitNumericIntType: Fractional[IntType] = intTypeFractional
  implicit val ImplicitNumericDoubleType: Fractional[DoubleType] = doubleTypeFractional
}