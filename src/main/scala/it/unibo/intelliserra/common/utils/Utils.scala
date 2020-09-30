package it.unibo.intelliserra.common.utils

import scala.util.Try

object Utils {

  def flattenTryIterable[B,C](iterable: Iterable[Try[B]])(ifFailure : Throwable => Unit)(ifSuccess : B => C): Iterable[C]  = {
    val (successes, failures) = iterable.partition(_.isSuccess)
    failures.map(_ => ifFailure)
    successes.flatMap(_.toOption).map(ifSuccess(_))
  }

}
