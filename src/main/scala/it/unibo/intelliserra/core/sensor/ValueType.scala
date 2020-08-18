package it.unibo.intelliserra.core.sensor

//TODO scaladoc
sealed trait ValueType

final case class StringType(value: String) extends ValueType
final case class IntType(value: Int) extends ValueType
final case class DoubleType(value: Double) extends ValueType
final case class CharType(value: Char) extends ValueType
final case class BooleanType(value: Boolean) extends ValueType
