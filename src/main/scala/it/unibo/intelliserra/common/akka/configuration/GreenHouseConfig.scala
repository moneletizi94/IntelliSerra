package it.unibo.intelliserra.common.akka.configuration

import com.typesafe.config.{Config, ConfigFactory}

object GreenHouseConfig {

  //noinspection ScalaStyle
  def apply(host: String = "localhost", port: Int = 8080): Config = {
    val properties = Map(
      "akka.remote.classic.netty.tcp.hostname" -> host,
      "akka.remote.classic.netty.tcp.port" -> port
    )
    createConfigWithFallback(properties)
  }

  def client(host: String = "localhost", port: Int = 0): Config = GreenHouseConfig(host, port)

  private def createConfigWithFallback(properties: Map[String, _]): Config = {
    val config = properties.map(kv => s"${kv._1}=${kv._2.toString}").mkString(",")
    ConfigFactory.parseString(config).withFallback(defaultConfig)
  }

  private val defaultConfig = ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    # provider=remote is possible, but prefer cluster
      |    provider = remote
      |    allow-java-serialization = true
      |    warn-about-java-serializer-usage = false
      |  }
      |  remote.artery.enabled = false
      |  remote.classic {
      |    enabled-transports = ["akka.remote.classic.netty.tcp"]
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |      port = 8080
      |    }
      | }
      |}
      |""".stripMargin)

}
