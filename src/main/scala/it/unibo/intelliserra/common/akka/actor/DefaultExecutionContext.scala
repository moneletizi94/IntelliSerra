package it.unibo.intelliserra.common.akka.actor

import akka.actor.Actor

import scala.concurrent.ExecutionContext

/**
 * Mixin trait for Actor that enable an implicit execution context
 */
trait DefaultExecutionContext { this: Actor =>
  implicit val executionContext: ExecutionContext = context.dispatcher
}
