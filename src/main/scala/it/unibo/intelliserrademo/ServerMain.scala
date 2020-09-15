package it.unibo.intelliserrademo

import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.ServerConfig.{AppConfig, RuleConfig, ZoneConfig}
import it.unibo.intelliserra.server.aggregation.Aggregator
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation._
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserrademo.CategoriesAndActions.Temperature

import scala.concurrent.duration._

object ServerMain extends App {
  val aggregators = List(Aggregator.createAggregator(Temperature)(sum))
  val rules = List()

  GreenHouseServer(ServerConfig(AppConfig("SerraDiPomodori", "localhost",8080),
                                ZoneConfig(5 seconds, 4 seconds, aggregators),
                                RuleConfig(rules)))
                                .start()

}
