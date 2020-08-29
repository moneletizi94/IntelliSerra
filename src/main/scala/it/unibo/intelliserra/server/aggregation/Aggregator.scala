package it.unibo.intelliserra.server.aggregation

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, NumericType, ValueType}

import scala.util.Try

trait Aggregator {
  def category : Category
  def aggregate(measures : List[Measure]) : Try[Measure]
}

object Aggregator{
  def createAggregator(category: Category)(implicit aggregateFunction : List[category.Value] => category.Value) : Aggregator =
    new BaseAggregator(category)(aggregateFunction)

  class BaseAggregator[T <: ValueType](override val category: Category)(val f : List[T] => T) extends Aggregator {
    override def aggregate(measures: List[Measure]): Try[Measure] = Try{ Measure(f(measures.map(_.value.asInstanceOf[T])), category) }
  }
}


object AggregateFunctions{
  import numericInt._
  def avg(implicit number : Fractional[IntType]) : List[IntType] => IntType = list => list.sum(number) / list.size
  def sum(implicit number : Fractional[IntType]) : List[IntType] => IntType = list => list.sum(number)


}
