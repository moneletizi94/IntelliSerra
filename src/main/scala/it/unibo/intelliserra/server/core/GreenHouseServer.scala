package it.unibo.intelliserra.server.core

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import it.unibo.intelliserra.common.akka.configuration.GreenHouseConfig
import it.unibo.intelliserra.server.core.GreenHouseActor.{ServerError, Start, Started}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

trait GreenHouseServer {
  def name: String
  def start(): Future[Unit]
  def terminate(): Future[Unit]
}

object GreenHouseServer {
  //noinspection ScalaStyle
  def apply(name: String,
            host: String = "localhost",
            port: Int = 8080): GreenHouseServer = new GreenHouseServerImpl(name, host, port)
  }

  private[server] class GreenHouseServerImpl(override val name: String,
                                             private val host: String,
                                             private val port: Int) extends GreenHouseServer {

    private val config = GreenHouseConfig(host, port)
    private implicit val timeout: Timeout = Timeout(5 seconds)
    private implicit val actorSystem: ActorSystem = ActorSystem(name, config)
    private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

    private val serverActor = GreenHouseActor(name)

    override def start(): Future[Unit] = {
      serverActor ? Start flatMap {
        case Started => Future.unit
        case ServerError(error) => Future.failed(error)
      }
    }

    override def terminate(): Future[Unit] = {
      actorSystem.stop(serverActor)
      actorSystem.terminate().flatMap(_ => Future.unit)
    }
}