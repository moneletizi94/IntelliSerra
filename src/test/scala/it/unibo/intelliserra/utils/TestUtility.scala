package it.unibo.intelliserra.utils

import akka.testkit.TestKit

import scala.concurrent.ExecutionContextExecutor

trait TestUtility {

  import akka.util.Timeout
  import scala.concurrent.duration.{Duration, _}
  import it.unibo.intelliserra.common.communication._

  val GREENHOUSE_NAME = "myserra"

  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val duration: FiniteDuration = 5 seconds
}