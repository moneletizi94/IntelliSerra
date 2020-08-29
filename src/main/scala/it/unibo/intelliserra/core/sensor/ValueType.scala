package it.unibo.intelliserra.core.sensor

//TODO scaladoc
sealed trait ValueType
sealed trait NumericType extends ValueType
sealed trait TextualType extends ValueType

/*implicit def fromIntToDouble
trait X[Y<:{val value:Int}] extends Numeric[Y]{
  override def plus(x: Y, y: Y): Y = x.x + y.x
  override def minus(x: IntType, y: IntType): Y = x.value - y.value
  override def times(x: IntType, y: IntType): Y = x.value * y.value
  override def negate(x: IntType): IntType = - x.value
  override def fromInt(x: Int): IntType = IntType(x)
  override def toInt(x: IntType): Int = x.value
  override def toLong(x: IntType): Long = x.value.toLong
  override def toFloat(x: IntType): Float = x.value.toFloat
  override def toDouble(x: IntType): Double = x.value.toDouble
  override def compare(x: IntType, y: IntType): Int = x.value compare y.value
}*/

final case class StringType(value: String) extends TextualType
final case class IntType(value: Int) extends NumericType
final case class DoubleType(value: Double) extends NumericType
final case class CharType(value: Char) extends TextualType
final case class BooleanType(value: Boolean) extends ValueType
