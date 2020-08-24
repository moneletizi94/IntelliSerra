package it.unibo.intelliserra.server.core

import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class GreenHouseServerSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with TestUtility {

  private var server: GreenHouseServer = _

  before {
    this.server = GreenHouseServer(GreenhouseName)
  }

  after {
    Await.result(this.server.terminate(), duration)
  }

  "Green house server facade " should {

    "allow to start server with success" in {
      Try(awaitReady(server.start())) match {
        case Failure(exception) => fail(exception)
        case Success(_) => succeed
      }
    }

    "raise IllegalStateException when start is called on already running instance" in {
      awaitResult(server.start())
      assertThrows[IllegalStateException] {
        awaitResult(server.start())
      }
    }
  }
}
