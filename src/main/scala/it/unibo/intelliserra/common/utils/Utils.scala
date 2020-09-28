package it.unibo.intelliserra.common.utils

import akka.actor.{Actor, ActorLogging}

import scala.util.Try

object Utils {

  def flattenTryIterable[B,C](iterable: Iterable[Try[B]])(ifFailure : Throwable => Unit)(ifSuccess : B => C): Iterable[C]  = {
    val (successes, failures) = iterable.partition(_.isSuccess)
    failures.map(_ => ifFailure)
    successes.flatMap(_.toOption).map(ifSuccess(_))
  }

  // TODO: togliere
  trait MessageReceivingLog { this : Actor with ActorLogging =>
      def logReceiving(message : Any) : Unit = {
        log.info(s"(${context.self.path.name}): Received message $message from ${context.sender.path.name}")
      }
  }

}
