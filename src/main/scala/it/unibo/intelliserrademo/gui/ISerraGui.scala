package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.rule.Rule
import it.unibo.intelliserra.examples.RuleDslExample.{Humidity, Temperature, Water}
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.core.GreenHouseServer
import it.unibo.intelliserra.core.rule.dsl._

import scala.swing._

// scalastyle:off magic.number
class ISerraGui(implicit client: GreenHouseClient) extends MainFrame {

  title = "IntelliSerra"
  preferredSize = new Dimension(800, 600)
  contents = new BoxPanel(Orientation.Vertical) {
    contents += ZonesComponent()
    contents += RuleComponent()
    contents += EntityComponent()
  }

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE)

}

object ISerraGui {

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "mySerra"
  val defaultServerConfig: ServerConfig = ServerConfig(GreenhouseName, Hostname, Port)
  val actionSet: Set[Action] = Set(Water)
  val rule: Rule = Temperature > 20 && Humidity < 10 executeMany actionSet
  val defaultConfigWithRule: ServerConfig = ServerConfig(GreenhouseName, Hostname, Port, rules = List(rule))

  def main(args: Array[String]): Unit = {
    val server = GreenHouseServer(defaultConfigWithRule)
    server.start()
    val client = GreenHouseClient(GreenhouseName, Hostname, Port)
    val gui = ISerraGui(client)
    gui.visible = true
  }

  def apply(implicit client: GreenHouseClient): ISerraGui = new ISerraGui
}


