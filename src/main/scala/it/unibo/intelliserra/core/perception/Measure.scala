package it.unibo.intelliserra.core.perception

/**
 * Represents wraps a sensed value with its category
 */
sealed trait Measure {
  /**
   * Type of the value sensed by a Sensor
   * @return [[it.unibo.intelliserra.core.perception.ValueType]]
   */
  def value: ValueType

  /**
   * Category of the value sensed by a Sensor
   * @return [[it.unibo.intelliserra.core.perception.Category]]
   */
  def category: Category[ValueType]
}

object Measure {
  /**
   * Create an implementation of a Measure with the specified
   * [[it.unibo.intelliserra.core.perception.Category]] and
   * [[it.unibo.intelliserra.core.perception.ValueType]]
   * @param category, specified category
   * @param value, specified value
   * @tparam V, specified valueType which must be the same for both category and value
   * @return a new Measure implementation
   */
  def apply[V <: ValueType](category: Category[V])(value: V): Measure = MeasureImpl(value, category)
  private case class MeasureImpl(override val value: ValueType, override val category: Category[ValueType]) extends Measure
}