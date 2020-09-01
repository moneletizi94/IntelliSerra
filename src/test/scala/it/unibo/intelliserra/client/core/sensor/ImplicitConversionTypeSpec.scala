package it.unibo.intelliserra.client.core.sensor

import it.unibo.intelliserra.core.sensor.{BooleanType, CharType, DoubleType, IntType, StringType}
import it.unibo.intelliserra.utils.TestImplicitConversionUtility
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.util.Success

// scalastyle:off magic.number
@RunWith(classOf[JUnitRunner])
class ImplicitConversionTypeSpec extends FlatSpec with Matchers with TestImplicitConversionUtility {

  "An implicit conversion" should "exists for each type" in {
    tryConvert[Int,IntType] _ shouldBe Success
    tryConvert[Double,DoubleType] _ shouldBe Success
    tryConvert[Long,DoubleType] _ shouldBe Success
    tryConvert[Float,DoubleType] _ shouldBe Success
    tryConvert[String,StringType] _ shouldBe Success
    tryConvert[Boolean, BooleanType] _ shouldBe Success
    tryConvert[Char, CharType] _ shouldBe Success
  }

}
