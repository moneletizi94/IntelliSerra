package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.gui.GuiUtility.createTextArea
import scala.swing.Action.NoAction.title
import scala.swing._
import scala.swing.event.ButtonClicked
import it.unibo.intelliserrademo.gui.util.SwingFuture._
import scala.concurrent.ExecutionContext.Implicits.global

// scalastyle:off magic.number
private[gui] class ZonePanel(zoneName: String)(implicit client: GreenHouseClient) extends GridPanel(1, 2) {

  title = zoneName
  hGap = 10
  private val textArea = createTextArea

  private val buttonAssign = new Button("AssignEntity")
  private val buttonState = new Button("GetState")
  private val buttonDelete = new Button("DeleteZone")

  contents += new GridPanel(3, 1) {
    vGap = 15
    contents += buttonAssign
    contents += buttonState
    contents += buttonDelete
  }
  contents += new ScrollPane(textArea)
  listenTo(buttonAssign)
  reactions += {
    case ButtonClicked(`buttonAssign`) => assignEntity()
    case ButtonClicked(`buttonDelete`) => ???
    case ButtonClicked(`buttonDelete`) => ???
  }

  /*
  mappare le future che sono eseguite in un altro thread che devo dire dove sono eseguite ( ex context) e poi mappate in swing
   */
  private def assignEntity(): Unit = {
    Dialog.showInput(contents.head, "Input name of entity to assign:", initial = "")
      .foreach(entityName => client.associateEntity(entityName, zoneName)//TODO CAMBIARE
        .safeSwingOnComplete(value => println(value)))
  }

}

object ZonePanel {
  def apply(zoneName: String)(implicit client: GreenHouseClient): ZonePanel = new ZonePanel(zoneName)
}
