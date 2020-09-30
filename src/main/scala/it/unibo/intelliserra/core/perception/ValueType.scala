package it.unibo.intelliserra.core.perception

/**
 * Represents the types of data allowed for the generation of measures
 */
sealed trait ValueType

/**
 * Represents the numeric types; it includes Int and Double
 */
sealed trait NumericType extends ValueType

/**
 * Represents the textual types; it includes String and Char
 */
sealed trait TextualType extends ValueType

/** represents a wrapper for String primitive type */
final case class StringType(value: String) extends TextualType
/** represents a wrapper for Int primitive type */
final case class IntType(value: Int) extends NumericType
/** represents a wrapper for Double primitive type */
final case class DoubleType(value: Double) extends NumericType
/** represents a wrapper for Char primitive type */
final case class CharType(value: Char) extends TextualType
/** represents a wrapper for Boolean primitive type */
final case class BooleanType(value: Boolean) extends ValueType

