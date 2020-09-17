package it.unibo.intelliserrademo.server

import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.ServerConfig.{AppConfig, RuleConfig, ZoneConfig}
import it.unibo.intelliserra.server.aggregation.AggregateFunctions.{avg, max, moreFrequent}
import it.unibo.intelliserra.server.aggregation.Aggregator.createAggregator
import it.unibo.intelliserra.server.aggregation._
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserrademo.common.CategoriesAndActions.{AirTemperature, DayNight, Humidity, SoilMoisture}

import scala.concurrent.duration._

object ServerMain extends App {
  val aggregators = List(createAggregator(AirTemperature)(avg),
                          createAggregator(SoilMoisture)(avg),
                          createAggregator(Humidity)(max),
                          createAggregator(DayNight)(moreFrequent))

  GreenHouseServer(ServerConfig(AppConfig("SerraDiPomodori", "localhost",8082),
                                ZoneConfig(5 seconds, 4 seconds, aggregators),
                                RuleConfig(TomatoRules.rules)))
                                .start()

}
