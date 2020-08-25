package it.unibo.intelliserra.common.akka.actor

import akka.actor.Actor

import scala.concurrent.ExecutionContext

trait DefaultExecutionContext { this: Actor =>
  implicit val executionContext: ExecutionContext = context.dispatcher
}
