package it.unibo.intelliserra.common.utils

import scala.util.Try

object Utils {

  // TODO: group by? 
  def atMostOne[A,B](collection: Seq[A])(groupBy : A => B) : Boolean = collection.groupBy(groupBy(_)).forall(_._2.lengthCompare(1) == 0)

  def flattenIterableTry[B,C](iterable: Iterable[Try[B]])(ifFailure : Throwable => Unit)(ifSuccess : B => C): Iterable[C]  = {
    val (successes, failures) = iterable.partition(_.isSuccess)
    failures.map(_ => ifFailure)
    successes.flatMap(_.toOption).map(ifSuccess(_))
  }

}
