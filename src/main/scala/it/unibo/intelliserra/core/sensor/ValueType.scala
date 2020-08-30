package it.unibo.intelliserra.core.sensor

//TODO scaladoc
sealed trait ValueType
sealed trait NumericType extends ValueType
sealed trait TextualType extends ValueType

final case class StringType(value: String) extends TextualType
final case class IntType(value: Int) extends NumericType
final case class DoubleType(value: Double) extends NumericType
final case class CharType(value: Char) extends TextualType
final case class BooleanType(value: Boolean) extends ValueType

