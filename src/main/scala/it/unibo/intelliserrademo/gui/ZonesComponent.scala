package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.gui.util.GuiUtility.updateComboBox
import it.unibo.intelliserrademo.gui.util.SwingFuture._
import javax.swing.{BorderFactory, BoxLayout, ScrollPaneConstants}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.ScrollPane.BarPolicy
import scala.swing.event.{ButtonClicked, SelectionChanged}
import scala.swing.{BorderPanel, Button, ComboBox, Dialog, Dimension, FlowPanel, GridPanel, ScrollPane}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
private[gui] class ZonesComponent(implicit client: GreenHouseClient) extends BorderPanel {

  private val createZoneButton = new Button("Create Zone")
  private var zones: Map[String, ZonePanel] = Map()
  implicit val deleteBox: ComboBox[String] = new ComboBox(zones.keySet.toSeq)
  private val zonesWrapperPanel = new GridPanel(0,2){
    hGap = 30
    vGap = 30
  }

  zonesWrapperPanel.peer.setBorder(BorderFactory.createTitledBorder("Zones: "))

  deleteBox.peer.setBorder(BorderFactory.createTitledBorder("Delete a zone"))
  deleteBox.preferredSize = new Dimension(150,50)

  add(new FlowPanel {
    contents += createZoneButton
    contents += deleteBox
  }, BorderPanel.Position.North)

  add(new ScrollPane(zonesWrapperPanel), BorderPanel.Position.Center)

  listenTo(createZoneButton, deleteBox.selection)

  reactions += {
    case ButtonClicked(`createZoneButton`) => createZone()
    case SelectionChanged(`deleteBox`) => deleteZone(deleteBox.selection.item)

  }

  private def createZone(): Unit = {
    val zoneToCreate = Dialog.showInput(contents.head, "Zone name:", initial = "")
    zoneToCreate.foreach(zoneValue => {
      val zone = zoneValue.replaceAll(" ", "")
      if(zone.length > 0) {
        client.createZone(zone)
          .safeSwingOnComplete {
            case Failure(exception) => Dialog.showMessage(contents.head, exception.getMessage)
            case Success(value) => addZonePanel(value)
          }
      } else { Dialog.showMessage(contents.head, "Invalid parameter")}
    })
  }

  private def addZonePanel(zoneName: String): Unit = {
    val zonePanel = ZonePanel(zoneName)
    zones += zoneName -> zonePanel
    zonesWrapperPanel.contents += zonePanel
    updateComboBox(zones.keySet.toSeq)
  }

  private def deleteZone(zone: String): Unit = {
    client.removeZone(zone).safeSwingOnComplete {
      case Failure(exception) => Dialog.showMessage(contents.head, exception.getMessage)
      case Success(_) =>
        zonesWrapperPanel.contents -= zones(zone)
        zones -= zone
        updateComboBox(zones.keySet.toSeq)
        zonesWrapperPanel.repaint()
    }
  }
}

object ZonesComponent {
  def apply()(implicit client: GreenHouseClient): ZonesComponent = new ZonesComponent
}