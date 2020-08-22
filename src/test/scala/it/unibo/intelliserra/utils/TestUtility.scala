package it.unibo.intelliserra.utils

import akka.testkit.TestKit

import scala.concurrent.{Await, Awaitable, ExecutionContextExecutor, Future}

trait TestUtility {

  import akka.util.Timeout
  import scala.concurrent.duration.{Duration, _}
  import it.unibo.intelliserra.common.communication._

  val GREENHOUSE_NAME = "myserra"

  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val duration: FiniteDuration = 5 seconds

  def awaitResult[T](awaitable: Awaitable[T])(implicit duration: Duration): T = Await.result(awaitable, duration)
  def awaitReady[T](awaitable: Awaitable[T])(implicit duration: Duration): awaitable.type = Await.ready(awaitable, duration)
}