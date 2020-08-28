package it.unibo.intelliserra.client.core
/*
@RunWith(classOf[JUnitRunner])
class ClientSpec extends WordSpecLike
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with TestUtility {

  private val ZoneName = "zone1"

  private var client: GreenHouseClient = _
  private var server: GreenHouseServer = _

  before {
    server = GreenHouseServer(GreenhouseName, Hostname, Port)
    client = GreenHouseClient(GreenhouseName, Hostname, Port)

    awaitReady(server.start())
  }

  after {
    awaitReady(server.terminate())
  }

  "A client " should {

    "create a new zone" in {
      awaitResult(client.createZone(ZoneName)) shouldBe ZoneName
      awaitResult(client.zones()) shouldBe List(ZoneName)
    }

    "remove an existing zone" in {
      awaitReady(client.createZone(ZoneName))
      awaitResult(client.removeZone(ZoneName)) shouldBe ZoneName
      awaitResult(client.zones()) shouldBe List()
    }

    "get all available zones" in {
      awaitResult(client.zones()) shouldBe List()
    }

    "fail to create zone if already exist" in {
      awaitResult(client.createZone(ZoneName)) shouldBe ZoneName
      assertThrows[IllegalArgumentException] {
        awaitResult(client.createZone(ZoneName))
      }
    }

    "fail to create zone if server is down" in {
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.createZone(ZoneName))
      }
    }

    "fail to remove a non existing zone" in {
      assertThrows[IllegalArgumentException] {
        awaitResult(client.removeZone(ZoneName))
      }
    }

    "fail to remove zone if server is down" in {
      awaitReady(client.createZone(ZoneName))
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.removeZone(ZoneName))
      }
    }

    "fail to get zone if server is down" in {
      awaitReady(server.terminate())
      assertThrows[Exception] {
        awaitResult(client.zones())
      }
    }
  }

}*/