package it.unibo.intelliserrademo.common

import it.unibo.intelliserra.server.ServerConfig.AppConfig

object DefaultAppConfig {

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "SerraDiPomodori"

  def appConfig: AppConfig = AppConfig(GreenhouseName, Hostname, Port)
}
