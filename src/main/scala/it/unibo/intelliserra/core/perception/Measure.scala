package it.unibo.intelliserra.core.perception

//TODO scaladoc
sealed trait Measure {
  def value: ValueType
  def category: Category[ValueType]
}

object Measure {
  def apply[V <: ValueType](category: Category[V])(value: V): Measure = MeasureImpl(value, category)
  private case class MeasureImpl(override val value: ValueType, override val category: Category[ValueType]) extends Measure
}