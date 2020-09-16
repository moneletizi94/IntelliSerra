package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.ServerConfig.{AppConfig, RuleConfig, ZoneConfig}
import it.unibo.intelliserra.server.aggregation.Aggregator._
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation._
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserrademo.CategoriesAndActions.{Fan, Humidity, Light, LightToggle, OpenWindow, Pressure, Temperature, Water, Weather}
import it.unibo.intelliserra.core.rule.dsl._
import scala.concurrent.duration._

object ServerMain extends App {
  val aggregators = List(createAggregator(Temperature)(avg),
                          createAggregator(Humidity)(avg),
                          createAggregator(Weather)(moreFrequent))

  val rules : List[Rule] = List(Temperature > 20 execute Fan,
                                Humidity < 60 executeMany Set(Water, OpenWindow),
                                Weather =:= "CLOUD" && Humidity > 70 execute Light)

  GreenHouseServer(ServerConfig(AppConfig("SerraDiPomodori", "localhost",8080),
                                ZoneConfig(5 seconds, 4 seconds, aggregators),
                                RuleConfig(rules)))
                                .start()

}
