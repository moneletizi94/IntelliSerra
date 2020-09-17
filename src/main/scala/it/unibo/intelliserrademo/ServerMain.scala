package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.ServerConfig.{AppConfig, RuleConfig, ZoneConfig}
import it.unibo.intelliserra.server.aggregation.Aggregator._
import it.unibo.intelliserra.server.aggregation.AggregateFunctions._
import it.unibo.intelliserra.server.aggregation._
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserrademo.CategoriesAndActions.{AirTemperature, DayNight, Dehumidifies, Fan, Heat, Humidity, SoilMoisture, Water}
import it.unibo.intelliserra.core.rule.dsl._
import scala.concurrent.duration._

object ServerMain extends App {
  val aggregators = List(createAggregator(AirTemperature)(avg),
                          createAggregator(SoilMoisture)(avg),
                          createAggregator(Humidity)(max),
                          createAggregator(DayNight)(moreFrequent))

  val rules = List(DayNight =:= "day" && AirTemperature > 30.0 execute Fan(5 seconds), // bad fertilization, start fan for reduce temperature
                  DayNight =:= "night" && AirTemperature < 10.0 execute Heat(3 seconds), // fertilization problem, start heating
                  SoilMoisture < 45.0 execute Water(10 seconds), // water stress condition
                  Humidity < 50.0 execute Dehumidifies(false),
                  Humidity > 70.0 execute Dehumidifies(true))

  GreenHouseServer(ServerConfig(AppConfig("SerraDiPomodori", "localhost",8080),
                                ZoneConfig(5 seconds, 4 seconds, aggregators),
                                RuleConfig(rules)))
                                .start()

}
