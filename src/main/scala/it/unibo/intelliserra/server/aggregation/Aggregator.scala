package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.sensor.{Category, Measure, ValueType}

trait Aggregator {
  def category : Category
  def aggregate(measures : List[Measure]) : Measure
}



object Aggregator{
  /*def createAggregator[R<: ValueType](category: Category)(implicit aggregateFunction : List[category.Value] => R) : Aggregator =
    new BaseAggregator[category.Value, R](category)(aggregateFunction)

  class BaseAggregator[T<: ValueType, R<: ValueType](override val category: Category)(val f : List[T] => R) extends Aggregator {
    override def aggregate(measures: List[Measure]): Measure = Measure(f(measures.map(_.value.asInstanceOf[T])), category)
  }*/
  def createAggregator[T<: ValueType, R<: ValueType](category: Category)(implicit aggregateFunction : List[T] => R) : Aggregator =
    new BaseAggregator[T, R](category)(aggregateFunction)

  class BaseAggregator[T<: ValueType, R<: ValueType](override val category: Category)(val f : List[T] => R) extends Aggregator {
    override def aggregate(measures: List[Measure]): Measure = Measure(f(measures.map(_.value.asInstanceOf[T])), category)
  }
}

/*object AggregateFunctions{
  def sum :  List[ValueType] => ValueType =
}*/
