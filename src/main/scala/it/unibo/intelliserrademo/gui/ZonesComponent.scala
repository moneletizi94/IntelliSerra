package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.gui.util.SwingFuture._
import javax.swing.{BorderFactory, ScrollPaneConstants}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.ScrollPane.BarPolicy
import scala.swing.event.{ButtonClicked, SelectionChanged}
import scala.swing.{BorderPanel, Button, ComboBox, Dialog, Dimension, FlowPanel, ScrollPane}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
private[gui] class ZonesComponent(implicit client: GreenHouseClient) extends BorderPanel {

  private val createZoneButton = new Button("Create Zone")
  private var zones: Map[String, ZonePanel] = Map()
  private val deleteBox = new ComboBox(zones.keySet.toSeq)
  private val zonesWrapperPanel = new FlowPanel()

  deleteBox.peer.setBorder(BorderFactory.createTitledBorder("Delete a zone"))
  deleteBox.preferredSize = new Dimension(150,50)

  add(new FlowPanel {
    contents += createZoneButton
    contents += deleteBox
  }, BorderPanel.Position.North)

  private val scroll = new ScrollPane(zonesWrapperPanel)
  scroll.horizontalScrollBarPolicy = BarPolicy.wrap(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
  scroll.verticalScrollBarPolicy = BarPolicy.wrap(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
  add(zonesWrapperPanel, BorderPanel.Position.Center)

  listenTo(createZoneButton, deleteBox.selection)

  reactions += {
    case ButtonClicked(`createZoneButton`) => createZone()
    case SelectionChanged(`deleteBox`) => deleteZone(deleteBox.selection.item)

  }
  //TODO refactor Dialog.showInput
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
    deleteBox.peer.setModel(ComboBox.newConstantModel(zones.keySet.toSeq))
  }

  private def deleteZone(zone: String): Unit = {
    client.removeZone(zone).safeSwingOnComplete {
      case Failure(exception) => Dialog.showMessage(contents.head, exception.getMessage)
      case Success(_) =>
        zonesWrapperPanel.contents -= zones(zone)
        zones -= zone
        deleteBox.peer.setModel(ComboBox.newConstantModel(zones.keySet.toSeq))
        zonesWrapperPanel.repaint()
    }
  }

}

object ZonesComponent {
  def apply()(implicit client: GreenHouseClient): ZonesComponent = new ZonesComponent
}