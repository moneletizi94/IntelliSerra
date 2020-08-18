package it.unibo.intelliserra.core.sensor

//TODO scaladoc
trait Measure {
  def value: ValueType
  def category: Category
}
