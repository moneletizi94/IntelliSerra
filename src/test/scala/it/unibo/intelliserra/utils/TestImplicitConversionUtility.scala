package it.unibo.intelliserra.utils

import scala.util.{Failure, Success, Try}

trait TestImplicitConversionUtility {
  private case object NoConversion extends (Any => Nothing) {
    def apply(x: Any) : Nothing = sys.error("No conversion")
  }

  // Just for convenience so NoConversion does not escape the scope.
  private def noConversion: Any => Nothing = NoConversion

  def canConvert[A,B]()(implicit f: A => B = noConversion): Boolean = f ne NoConversion
  def tryConvert[A,B](a: A)(implicit f: A => B = noConversion): Try[B] = if (f eq NoConversion) Failure(new Exception("no implicit conversion found")) else Success(f(a))
  def optConvert[A,B](a: A)(implicit f: A => B = noConversion): Option[B] = if (f ne NoConversion) Option(f(a)) else None
}
