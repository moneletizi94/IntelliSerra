package it.unibo.intelliserra.common.utils

import java.util

import akka.actor.AbstractActor.ActorContext
import akka.actor.{Actor, ActorLogging}
import it.unibo.intelliserra.server.aggregation.Aggregator

import scala.util.Try

object Utils {

  def flattenTryIterable[B,C](iterable: Iterable[Try[B]])(ifFailure : Throwable => Unit)(ifSuccess : B => C): Iterable[C]  = {
    val (successes, failures) = iterable.partition(_.isSuccess)
    failures.map(_ => ifFailure)
    successes.flatMap(_.toOption).map(ifSuccess(_))
  }

  trait MessageReceivingLog { this : Actor with ActorLogging =>
      def logReceiving(message : Any) : Unit = {
        log.info(s"(${context.self.path.name}): Received message $message from ${context.sender.path.name}")
      }
  }
  /*
    ALTERNATIVELY:
    akka.remote.artery {
      # If this is "on", Akka will log all inbound messages at DEBUG level,
      # if off then they are not logged
      log-received-messages = on
    }
   */

}
