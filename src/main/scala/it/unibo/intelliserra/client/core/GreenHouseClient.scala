package it.unibo.intelliserra.client.core

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.RemotePath
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.common.communication.Protocol._
import it.unibo.intelliserra.core.state.State

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait GreenHouseClient extends ZoneClient {
  /**
  * Remove an entity, either or not associated to a zone
  * @param entity the entity to remove
    * @return
  */
  def removeEntity(entity: Entity): Future[Entity]
}

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

    override def dissociateEntity(entity: Entity): Future[Entity] = (client ? DissociateEntity(entity)).mapTo[Entity]

    override def getState(zone: Zone): Future[Option[State]] = (client ? GetState(zone)).mapTo[Option[State]]

    override def removeEntity(entity: Entity): Future[Entity] = (client ? RemoveEntity(entity)).mapTo[Entity]
  }

}