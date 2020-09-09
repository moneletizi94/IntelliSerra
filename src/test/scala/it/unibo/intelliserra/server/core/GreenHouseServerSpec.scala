package it.unibo.intelliserra.server.core

import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Protocol.{GetZones, ServiceResponse}
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class GreenHouseServerSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with TestUtility {

  private var server: GreenHouseServer = _

  before {
    this.server = GreenHouseServer(defaultServerConfig)
  }

  after {
    awaitReady(this.server.terminate())
  }

  "Green house server facade " should {

    "allow to start server with success" in {
      awaitReady(server.start())
    }

    "allow to terminate server with success" in {
      awaitReady(server.start())
      awaitReady(server.terminate())
      assertThrows[TimeoutException] {
        awaitResult(makeTestRequest())
      }
    }

    "raise IllegalStateException when start is called on already running instance" in {
      awaitResult(server.start())
      assertThrows[IllegalStateException] {
        awaitResult(server.start())
      }
    }

    "raise TimeoutException when start() is called on terminated instance" in {
      awaitResult(server.start())
      awaitResult(server.terminate())
      assertThrows[TimeoutException] {
        awaitResult(server.start())
      }
    }
  }

  // Send a test request to server
  private def makeTestRequest(): Future[ServiceResponse] = {
    import akka.pattern.ask
    val system = ActorSystem("request", GreenHouseConfig.client())
    implicit val ec: ExecutionContext = system.dispatcher
    val server = system actorSelection RemotePath.server(GreenhouseName, Hostname, Port)
    (server ? GetZones())
      .mapTo[ServiceResponse]
      .transform {
        case Failure(exception) => system.terminate(); Failure(exception)
        case Success(response) => system.terminate(); Success(response)
      }
  }
}
