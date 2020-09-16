package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserra.server.ServerConfig
import it.unibo.intelliserra.server.core.GreenHouseServer

import scala.swing._

// scalastyle:off magic.number
class ISerraGui(implicit client: GreenHouseClient) extends MainFrame {

  title = "IntelliSerra"
  preferredSize = new Dimension(320, 240)
  contents = new BoxPanel(Orientation.Vertical) {
    contents += new ZonePanel("zone1")
  }

}

object ISerraGui {

  val Hostname = "localhost"
  val Port = 8080
  val GreenhouseName = "mySerra"
  val defaultServerConfig: ServerConfig = ServerConfig(GreenhouseName, Hostname, Port)

  def main(args: Array[String]): Unit = {
    val server = GreenHouseServer(defaultServerConfig)
    server.start()
    val client = GreenHouseClient(GreenhouseName, Hostname, Port)
    val gui = ISerraGui(client)
    gui.visible = true
  }

  def apply(implicit client: GreenHouseClient): ISerraGui = new ISerraGui
}


