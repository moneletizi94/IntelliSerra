package it.unibo.intelliserra.server.core

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.actor.{DefaultExecutionContext, DefaultTimeout}
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, ServerResponse, Start, Started, Stop, Stopped}

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
   * Create a new greenhouse server with specified configuration.
   * @return an instance of a [[it.unibo.intelliserra.server.core.GreenHouseServer]]
   */
  // scalastyle:off magic.number
  def apply(serverConfig: ServerConfig): GreenHouseServer = new GreenHouseServerImpl(serverConfig)

  /**
   * Implementation of a greenhouse server. It uses Akka ActorSystem as a server
   * @param serverConfig use
   */
  private[core] class GreenHouseServerImpl(serverConfig: ServerConfig) extends GreenHouseServer {

    override val name: String = serverConfig.appConfig.name
    private val host: String = serverConfig.appConfig.host
    private val port: Int = serverConfig.appConfig.port
    private val config = GreenHouseConfig(host, port)

    private implicit val timeout: Timeout = Timeout(5 seconds)
    private implicit val actorSystem: ActorSystem = ActorSystem(name, config)
    private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    private val serverActor = GreenHouseActor(serverConfig.ruleConfig, serverConfig.zoneConfig)

    override def start(): Future[Unit] =
      (serverActor ? Start)
        .mapTo[ServerResponse]
        .flatMap {
          case Started => Future.unit
          case ServerError(error) => Future.failed(error)
          case _ => Future.failed(new Exception("unknown error"))
        }

    override def terminate(): Future[Unit] = {
      (serverActor ? Stop)
        .mapTo[ServerResponse]
        .flatMap {
          case Stopped => actorSystem.terminate().flatMap(_ => Future.unit)
          case ServerError(error) => Future.failed(error)
          case _ => Future.failed(new Exception("unknown error"))
        }
    }
  }
}