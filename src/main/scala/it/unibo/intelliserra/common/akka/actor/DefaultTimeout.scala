package it.unibo.intelliserra.common.akka.actor

import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._

trait DefaultTimeout { this: Actor =>
  implicit val timeout: Timeout =  Timeout(5 seconds)
}
