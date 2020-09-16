package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.ServerConfig.{AppConfig, RuleConfig, ZoneConfig}
import it.unibo.intelliserra.server.aggregation.Aggregator._
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation._
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserrademo.CategoriesAndActions.{AirTemperature, DayNight, Fan, Humidity, Light, OpenWindow, SoilMoisture, Water, Weather}

import scala.concurrent.duration._

object ServerMain extends App {
  val aggregators = List(createAggregator(AirTemperature)(avg),
                          createAggregator(SoilMoisture)(avg),
                          createAggregator(Humidity)(max),
                          createAggregator(DayNight)(moreFrequent))

  GreenHouseServer(ServerConfig(AppConfig("SerraDiPomodori", "localhost",8080),
                                ZoneConfig(5 seconds, 4 seconds, aggregators),
                                RuleConfig(TomatoRules.rules)))
                                .start()

}
