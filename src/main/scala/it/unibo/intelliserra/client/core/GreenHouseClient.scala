package it.unibo.intelliserra.client.core

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Protocol._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

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

    override def createZone(zone: Zone): Future[Zone] =
      client ? CreateZone(zone) flatMap {
        case Success(_) => Future.successful(zone)
        case Failure(ex) => Future.failed(ex)
      }

    override def removeZone(zone: Zone): Future[Zone] =
      client ? RemoveZone(zone) flatMap {
        case Success(_) => Future.successful(zone)
        case Failure(ex) => Future.failed(ex)
      }
  }

}