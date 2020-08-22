package it.unibo.intelliserra.client.core

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import it.unibo.intelliserra.client.core.Client.ClientImpl
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class ClientSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private var client: GreenHouseClient = _
  private var server: GreenHouseServer = _
  private val zoneName: String = "zone1"

  before {
    server = GreenHouseServer(GREENHOUSE_NAME, "localhost", 8081)
    client = GreenHouseClient(RemotePath.server(GREENHOUSE_NAME, "localhost", 8081))

    Await.ready(server.start(), duration)
  }

  after {
    Await.ready(server.terminate(), duration)
  }

  "A client " should {

    "create a new zone" in {
      awaitResult(client.createZone(zoneName)) shouldBe zoneName
      // TODO: verify with get zones
    }

    "fail to create zone if already exist" in {
      awaitResult(client.createZone(zoneName)) shouldBe zoneName
      assertThrows[IllegalStateException] {
        awaitResult(client.createZone(zoneName))
      }
    }

    "fail to create zone if server is down" in {
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.createZone(zoneName))
      }
    }
  }

}