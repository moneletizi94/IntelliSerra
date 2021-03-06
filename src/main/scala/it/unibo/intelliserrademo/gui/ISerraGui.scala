package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.common.DefaultAppConfig

import scala.swing._

// scalastyle:off magic.number
class ISerraGui(implicit client: GreenHouseClient) extends MainFrame {

  title = "IntelliSerra"
  preferredSize = new Dimension(900, 600)
  resizable = false
  contents = new BoxPanel(Orientation.Vertical) {
    contents += ZonesComponent()
    contents += RuleComponent()
    contents += EntityComponent()
  }

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE)

}

object ISerraGui {

  def main(args: Array[String]): Unit = {
    val client = GreenHouseClient(DefaultAppConfig.GreenhouseName, DefaultAppConfig.Hostname, DefaultAppConfig.Port)
    val gui = ISerraGui(client)
    gui.visible = true
  }

  def apply(implicit client: GreenHouseClient): ISerraGui = new ISerraGui
}


