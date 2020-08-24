package it.unibo.intelliserra.utils

import scala.concurrent.{Await, Awaitable}

trait TestUtility {

  import akka.util.Timeout
  import scala.concurrent.duration.{Duration, _}
  import it.unibo.intelliserra.common.communication._

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "mySerra"

  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val duration: FiniteDuration = 5 seconds

  def awaitResult[T](awaitable: Awaitable[T])(implicit duration: Duration): T = Await.result(awaitable, duration)
  def awaitReady[T](awaitable: Awaitable[T])(implicit duration: Duration): awaitable.type = Await.ready(awaitable, duration)
}