package it.unibo.intelliserra.client.core

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Protocol.{AssignEntity, CreateZone, DeleteZone, GetZones}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait GreenHouseClient extends ZoneClient

object GreenHouseClient {

  /**
   * Create a new instance of GreenHouseClient
   * @param greenHouseName  the name of greenhouse
   * @param serverAddress   the address of server
   * @param serverPort      the port of server
   * @return a new instance of client
   */
  def apply(greenHouseName: String, serverAddress: String, serverPort: Int): GreenHouseClient =
    new GreenHouseClientImpl(greenHouseName, serverAddress, serverPort)

  private[core] class GreenHouseClientImpl(private val greenHouseName: String,
                                           private val serverAddress: String,
                                           private val serverPort: Int) extends GreenHouseClient {

    private implicit val actorSystem: ActorSystem = ActorSystem("client", GreenHouseConfig.client())
    private implicit val timeout: Timeout = Timeout(5 seconds)
    private implicit val executionContext: ExecutionContext = actorSystem.dispatcher

    private val client = Client(RemotePath.server(greenHouseName, serverAddress, serverPort))

    override def createZone(zone: Zone): Future[Zone] = (client ? CreateZone(zone)).mapTo[Zone]

    override def removeZone(zone: Zone): Future[Zone] = (client ? DeleteZone(zone)).mapTo[Zone]

    override def zones(): Future[List[Zone]] = (client ? GetZones()).mapTo[List[Zone]]

    override def associateEntity(entity: Entity, zone: Zone): Future[Zone] = (client ? AssignEntity(zone, entity)).mapTo[Zone]
  }

}