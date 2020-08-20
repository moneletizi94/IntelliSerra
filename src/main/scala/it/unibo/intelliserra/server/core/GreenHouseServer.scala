package it.unibo.intelliserra.server.core

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, ServerResponse, Start, Started}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

sealed trait GreenHouseServer {

  /**
   * The logical name of green house server
   */
  def name: String

  /**
   * Start the server
   * @return A future that complete when the server is started
   */
  def start(): Future[Unit]

  /**
   * Terminate the server permanently
   * @return A future that complete when the server is terminated
   */
  def terminate(): Future[Unit]
}

object GreenHouseServer {

  /**
   * Create a new greenhouse server with specified name at the specified host and port.
   *
   * @param name  the name of GreenHouse instance
   * @param host  the hostname of the server
   * @param port  the port of the server
   * @return an instance of a [[it.unibo.intelliserra.server.core.GreenHouseServer]]
   */
  //noinspection ScalaStyle
  def apply(name: String,
            host: String = "localhost",
            port: Int = 8080): GreenHouseServer = new GreenHouseServerImpl(name, host, port)
  }

  /**
   * Implementation of a greenhouse server. It uses Akka ActorSystem as a server
   * @param name  the name of GreenHouse instance
   * @param host  the hostname of the server
   * @param port  the port of the server
   */
  private[server] class GreenHouseServerImpl(override val name: String,
                                             private val host: String,
                                             private val port: Int) extends GreenHouseServer {

    private val config = GreenHouseConfig(host, port)

    private implicit val timeout: Timeout = Timeout(5 seconds)
    private implicit val actorSystem: ActorSystem = ActorSystem(name, config)
    private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    private val serverActor = GreenHouseActor(name)

    override def start(): Future[Unit] =
      (serverActor ? Start)
        .asInstanceOf[Future[ServerResponse]]
        .flatMap {
          case Started => Future.unit
          case ServerError(error) => Future.failed(error)
        }

    override def terminate(): Future[Unit] = {
      actorSystem.stop(serverActor)
      actorSystem.terminate().flatMap(_ => Future.unit)
    }
}