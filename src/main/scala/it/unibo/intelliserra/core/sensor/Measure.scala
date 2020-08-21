package it.unibo.intelliserra.core.sensor

//TODO scaladoc
sealed trait Measure {
  def value: ValueType
  def category: Category
}

object Measure {
  def apply(value: ValueType, category: Category): Measure = MeasureImpl(value, category)
  private case class MeasureImpl(override val value: ValueType, override val category: Category) extends Measure
}
