package it.unibo.intelliserra.server.core.sensor

import it.unibo.intelliserra.core.perception.{BooleanType, CharType, DoubleType, IntType, StringType}
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
    tryConvert[Double,DoubleType](2.0).isSuccess shouldBe true
    tryConvert[Long,DoubleType](2L).isSuccess shouldBe true
    tryConvert[Float,DoubleType](2F).isSuccess shouldBe true
    tryConvert[String,StringType](" ").isSuccess shouldBe true
    tryConvert[Boolean, BooleanType](false).isSuccess shouldBe true
    tryConvert[Char, CharType]('c').isSuccess shouldBe true
  }

}
