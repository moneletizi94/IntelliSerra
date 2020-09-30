package it.unibo.intelliserra.common.akka.configuration

import com.typesafe.config.{Config, ConfigFactory}

/**
 * Factory for akka actor system configuration
 */
object GreenHouseConfig {

  /**
   * Create a server configuration for akka remote actor system
   * @param host  the hostname of server
   * @param port  the port of server
   * @return a server configuration
   */
  def apply(host: String = "localhost", port: Int = 8080): Config = { // scalastyle:off magic.number
    val properties = Map(
      "akka.remote.classic.netty.tcp.hostname" -> host,
      "akka.remote.classic.netty.tcp.port" -> port
    )
    createConfigWithFallback(properties)
  }

  /**
   * Create a client configuration for akka remote actor system
   * @param host  the hostname where server is running
   * @param port  the port where server is running
   * @return a client configuration
   */
  def client(host: String = "localhost", port: Int = 0): Config = GreenHouseConfig(host, port)

  private def createConfigWithFallback(properties: Map[String, _]): Config = {
    val config = properties.map(kv => s"${kv._1}=${kv._2.toString}").mkString(",")
    ConfigFactory.parseString(config).withFallback(defaultConfig)
  }

  // default akka configuration
  private val defaultConfig = ConfigFactory.parseString(
    """
      |akka {
      |  loglevel = "DEBUG"
      |  actor {
      |    # provider=remote is possible, but prefer cluster
      |    provider = remote
      |    allow-java-serialization = true
      |    warn-about-java-serializer-usage = false
      |    debug {
      |      # enable DEBUG logging of all AutoReceiveMessages (Kill, PoisonPill etc.)
      |      receive = on
      |    }
      |  }
      |  remote.artery.enabled = false
      |  remote.classic {
      |    # If this is "on", Akka will log all inbound messages at DEBUG level,
      |    # if off then they are not logged
      |    log-received-messages = on
      |    enabled-transports = ["akka.remote.classic.netty.tcp"]
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      port = 8080
      |    }
      | }
      |}
      |""".stripMargin)

}
