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
    tryConvert[Int,IntType](1).isSuccess shouldBe true
    tryConvert[Double,DoubleType](1.0).isSuccess shouldBe true
    tryConvert[Long,DoubleType](1L).isSuccess shouldBe true
    tryConvert[Float,DoubleType](1F).isSuccess shouldBe true
    tryConvert[String,StringType]("").isSuccess shouldBe true
    tryConvert[Boolean, BooleanType](true).isSuccess shouldBe true
    tryConvert[Char, CharType]('c').isSuccess shouldBe true
  }

}
