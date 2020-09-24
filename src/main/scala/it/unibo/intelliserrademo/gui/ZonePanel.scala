package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.gui.util.GuiUtility.createTextArea
import it.unibo.intelliserrademo.gui.util.StatePrintify
import it.unibo.intelliserrademo.gui.util.SwingFuture._
import javax.swing.BorderFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing._
import scala.swing.event.ButtonClicked

// scalastyle:off magic.number
private[gui] class ZonePanel(zoneName: String)(implicit client: GreenHouseClient) extends GridPanel(1, 2) {

  peer.setBorder(BorderFactory.createTitledBorder(zoneName))

  hGap = 10
  private val textArea = createTextArea

  private val buttonAssign = new Button("AssignEntity")
  private val buttonState = new Button("GetState")

  contents += new GridPanel(2, 1) {
    vGap = 15
    contents += buttonAssign
    contents += buttonState
  }
  contents += new ScrollPane(textArea)
  listenTo(buttonAssign)
  listenTo(buttonState)


  reactions += {
    case ButtonClicked(`buttonAssign`) => assignEntity()
    case ButtonClicked(`buttonState`) => exposeState()
  }

  /*
  mappare le future che sono eseguite in un altro thread che devo dire dove sono eseguite ( ex context) e poi mappate in swing
   */
  private def assignEntity(): Unit = {
    Dialog.showInput(contents.head, "Input name of entity to assign:", initial = "")
      .foreach(entityName => client.associateEntity(entityName, zoneName)
        .safeSwingOnCompleteValue(value => textArea.append(value + "\n")))
  }

  private def exposeState(): Unit = {
    client.getState(zoneName)
      .safeSwingOnComplete(tried => tried
        .foreach(state => textArea.append(StatePrintify.statePrintify(state) + "\n")))
  }
}

object ZonePanel {
  def apply(zoneName: String)(implicit client: GreenHouseClient): ZonePanel = new ZonePanel(zoneName)
}
