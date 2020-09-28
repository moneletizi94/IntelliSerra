package it.unibo.intelliserra.server

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.NumberType
import it.unibo.intelliserra.core.perception.{DoubleType, IntType}

import scala.collection.LinearSeq
import scala.math.Fractional

package object aggregation {

  implicit class RichTraversable[T](traversable: Traversable[T]){
    def avg(implicit fractional: Fractional[T]) : T = fractional.mkNumericOps(traversable.sum(fractional)) / fractional.fromInt(traversable.size)
    def computeFrequency : Map[T,Int] = traversable.groupBy(identity).mapValues(_.size)
  }

  implicit class RichSeq[T](seq: LinearSeq[T]){
    def hasUniqueValueForProperty[B](property : T => B) : Boolean = seq.groupBy(property(_)).forall(_._2.lengthCompare(1) == 0)
  }


  val intTypeFractional: Fractional[IntType] = new Fractional[IntType] {
    override def plus(x: IntType, y: IntType): IntType = x.value + y.value

    override def minus(x: IntType, y: IntType): IntType = x.value - y.value

    override def times(x: IntType, y: IntType): IntType = x.value * y.value

    override def negate(x: IntType): IntType = - x.value

    override def fromInt(x: Int): IntType = IntType(x)

    override def toInt(x: IntType): Int = x.value

    override def toLong(x: IntType): Long = x.value.toLong

    override def toFloat(x: IntType): Float = x.value.toFloat

    override def toDouble(x: IntType): Double = x.value.toDouble

    override def compare(x: IntType, y: IntType): Int = x.value compare y.value

    override def div(x: IntType, y: IntType): IntType = x.value / y.value
  }

  val doubleTypeFractional: Fractional[DoubleType] = new Fractional[DoubleType] {
    override def plus(x: DoubleType, y: DoubleType): DoubleType = x.value + y.value

    override def minus(x: DoubleType, y: DoubleType): DoubleType = x.value - y.value

    override def times(x: DoubleType, y: DoubleType): DoubleType = x.value * y.value

    override def negate(x: DoubleType): DoubleType = - x.value

    override def fromInt(x: Int): DoubleType = DoubleType(x)

    override def toInt(x: DoubleType): Int = x.value.toInt

    override def toLong(x: DoubleType): Long = x.value.toLong

    override def toFloat(x: DoubleType): Float = x.value.toFloat

    override def toDouble(x: DoubleType): Double = x.value

    override def compare(x: DoubleType, y: DoubleType): Int = x.value compare y.value

    override def div(x: DoubleType, y: DoubleType): DoubleType = x.value / y.value
  }

}
