package it.unibo.intelliserra.server.aggregation

import it.unibo.intelliserra.core.perception.{DoubleType, IntType}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class NumericImplementationsSpec extends FlatSpec with Matchers{

  "A numeric integer " should " do operations correctly" in {
    intTypeFractional.plus(3,3) shouldBe IntType(6)
    intTypeFractional.minus(3,3) shouldBe IntType(0)
    intTypeFractional.times(3,3) shouldBe IntType(9)
    intTypeFractional.negate(3) shouldBe IntType(-3)
    intTypeFractional.fromInt(3) shouldBe IntType(3)
    intTypeFractional.toInt(3) shouldBe 3
    intTypeFractional.toLong(3) shouldBe 3L
    intTypeFractional.toFloat(3) shouldBe 3.0
    intTypeFractional.toDouble(3) shouldBe 3.0
    intTypeFractional.compare(3,3) shouldBe 0
    intTypeFractional.div(3,3) shouldBe IntType(1)
  }

  "A numeric double " should " do operations correctly" in {
    doubleTypeFractional.plus(3.0,3.0) shouldBe DoubleType(6.0)
    doubleTypeFractional.minus(3.0,3.0) shouldBe DoubleType(0)
    doubleTypeFractional.times(3.0,3.0) shouldBe DoubleType(9)
    doubleTypeFractional.negate(3.0) shouldBe DoubleType(-3)
    doubleTypeFractional.fromInt(3) shouldBe DoubleType(3)
    doubleTypeFractional.toInt(3.0) shouldBe 3
    doubleTypeFractional.toLong(3.0) shouldBe 3L
    doubleTypeFractional.toFloat(3.0) shouldBe 3.0
    doubleTypeFractional.toDouble(3.0) shouldBe 3.0
    doubleTypeFractional.compare(3.0,3) shouldBe 0
    doubleTypeFractional.div(3.0,3) shouldBe DoubleType(1)
  }

}
