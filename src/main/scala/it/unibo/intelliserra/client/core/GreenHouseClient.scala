package it.unibo.intelliserra.client.core

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.server.core.GreenHouseServer

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

trait GreenHouseClient extends ZoneClient {
  override type Zone = String
}

trait ZoneClient {
  type Zone
  def createZone(zone: Zone): Future[Zone]
  def removeZone(zone: Zone): Future[Zone]
}

object GreenHouseClient {

  def apply(serverUri: String): GreenHouseClient = new GreenHouseClientImpl(serverUri)

  private[core] class GreenHouseClientImpl(serverUri: String) extends GreenHouseClient {

    private implicit val actorSystem: ActorSystem = ActorSystem("client", GreenHouseConfig.client())
    private implicit val timeout: Timeout = Timeout(5 seconds)
    private implicit val executionContext: ExecutionContext = actorSystem.dispatcher

    private val client = Client(serverUri)

    override def createZone(zone: Zone): Future[Zone] =
      client ? CreateZone(zone) flatMap {
        case Success(_) => Future.successful(zone)
        case Failure(ex) => Future.failed(ex)
      }

    override def removeZone(zone: Zone): Future[Zone] = ???
  }

}