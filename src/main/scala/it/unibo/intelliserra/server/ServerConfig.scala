package it.unibo.intelliserra.server

import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.server.ServerConfig._
import it.unibo.intelliserra.server.aggregation.Aggregator
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

/**
 * This is the server configuration where there are all the configuration info for the server
 * @param appConfig it is to setup the application (host, port, name
 * @param zoneConfig it is for [[it.unibo.intelliserra.server.zone.ZoneActor]]
 * @param ruleConfig it is for RuleEngineService
 */
final case class ServerConfig private(appConfig: AppConfig, zoneConfig: ZoneConfig, ruleConfig: RuleConfig)

object ServerConfig {

  // scalastyle:off magic.number
  // TODO: scaladoc 
  def apply(name: String,
            host: String = "localhost",
            port: Int = 8080,
            actionsEvaluationPeriod: FiniteDuration = 10 seconds,
            stateEvaluationPeriod: FiniteDuration = 5 seconds,
            aggregators: List[Aggregator] = List(),
            rules: List[Rule] = List()): ServerConfig = {

    val appConfig = AppConfig(name, host, port)
    val ruleConfig = RuleConfig(rules)
    val zoneConfig = ZoneConfig(actionsEvaluationPeriod, stateEvaluationPeriod,aggregators)

    ServerConfig(appConfig, zoneConfig, ruleConfig)
  }

  /**
   * Represents application configuration
   * @param name name of the application
   * @param host server host
   * @param port number of the port for the server
   */
  final case class AppConfig private(name: String,
                                     host: String,
                                     port: Int)

  /**
   * Represents zone configuration
   * @param actionsEvaluationPeriod used to check periodically for new actions to execute,
   *                                asking to rule engine
   * @param stateEvaluationPeriod used to update zone internal state
   * @param aggregators used to aggregate same measures came from different sensors
   */
  final case class ZoneConfig private(actionsEvaluationPeriod: FiniteDuration,
                                      stateEvaluationPeriod: FiniteDuration,
                                      aggregators: List[Aggregator])

  /**
   * Represents rule Engine configuration
   * @param rules list of rules
   */
  final case class RuleConfig private(rules: List[Rule])
}