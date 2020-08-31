package it.unibo.intelliserra.client.core.sensor

import it.unibo.intelliserra.core.sensor.{BooleanType, CharType, DoubleType, IntType, StringType}
import it.unibo.intelliserra.utils.{TestImplicitConversionUtility, TestUtility}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class ImplicitConversionTypeSpec extends FlatSpec with Matchers with TestImplicitConversionUtility {

  "An implicit conversion" should "exists for each type" in {
    canConvert[Int,IntType] shouldBe true
    canConvert[Double,DoubleType] shouldBe true
    canConvert[Long,DoubleType] shouldBe true
    canConvert[Float,DoubleType] shouldBe true
    canConvert[String,StringType] shouldBe true
    canConvert[Boolean, BooleanType] shouldBe true
    canConvert[Char, CharType] shouldBe true
  }

}
