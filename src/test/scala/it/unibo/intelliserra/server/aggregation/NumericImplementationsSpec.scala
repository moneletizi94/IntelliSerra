package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.sensor.{IntType, ValueType}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import it.unibo.intelliserra.core.sensor._

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class NumericImplementationsSpec extends FlatSpec with Matchers{

  import numericInt._
  import numericDouble._

  "A numeric integer " should " do operations correctly" in {
    numericInt.plus(3,3) shouldBe IntType(6)
    numericInt.minus(3,3) shouldBe IntType(0)
    numericInt.times(3,3) shouldBe IntType(9)
    numericInt.negate(3) shouldBe IntType(-3)
    numericInt.fromInt(3) shouldBe IntType(3)
    numericInt.toInt(3) shouldBe 3
    numericInt.toLong(3) shouldBe 3L
    numericInt.toFloat(3) shouldBe 3.0
    numericInt.toDouble(3) shouldBe 3.0
    numericInt.compare(3,3) shouldBe 0
    numericInt.div(3,3) shouldBe IntType(1)
  }

  "A numeric double " should " do operations correctly" in {
    numericDouble.plus(3.0,3.0) shouldBe DoubleType(6.0)
    numericDouble.minus(3.0,3.0) shouldBe DoubleType(0)
    numericDouble.times(3.0,3.0) shouldBe DoubleType(9)
    numericDouble.negate(3.0) shouldBe DoubleType(-3)
    numericDouble.fromInt(3) shouldBe DoubleType(3)
    numericDouble.toInt(3.0) shouldBe 3
    numericDouble.toLong(3.0) shouldBe 3L
    numericDouble.toFloat(3.0) shouldBe 3.0
    numericDouble.toDouble(3.0) shouldBe 3.0
    numericDouble.compare(3.0,3) shouldBe 0
    numericDouble.div(3.0,3) shouldBe DoubleType(1)
  }

}
