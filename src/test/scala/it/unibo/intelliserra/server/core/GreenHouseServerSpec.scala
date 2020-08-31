package it.unibo.intelliserra.server.core

import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class GreenHouseServerSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with TestUtility {

  private var server: GreenHouseServer = _
  private val aggregators: List[Aggregator] = List()

  before {
    this.server = GreenHouseServer(GreenhouseName)
  }

  after {
    Await.result(this.server.terminate(), duration)
  }

  "Green house server facade " should {

    "allow to start server with success" in {
      awaitReady(server.start(aggregators))
    }

    "raise IllegalStateException when start is called on already running instance" in {
      awaitResult(server.start(aggregators))
      assertThrows[IllegalStateException] {
        awaitResult(server.start(aggregators))
      }
    }
  }
}
