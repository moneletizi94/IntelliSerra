package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.gui.util.GuiUtility._
import it.unibo.intelliserrademo.gui.util.RulePrintify._
import it.unibo.intelliserrademo.gui.util.SwingFuture._
import javax.swing.BorderFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.event.ButtonClicked
import scala.swing.{Button, ComboBox, Dimension, FlowPanel}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
private[gui] class RuleComponent(implicit client: GreenHouseClient) extends FlowPanel {

  private var rulesInfo: Map[String, String] = Map()
  implicit val rulesBox: ComboBox[String] = new ComboBox(rulesInfo.values.toSeq)
  private val enableButton = new Button("Enable rule")
  private val disableButton = new Button("Disable rule")

  loadRules()

  rulesBox.peer.setBorder(BorderFactory.createTitledBorder("Rules"))
  rulesBox.preferredSize = new Dimension(300, 50)

  contents += rulesBox
  contents += enableButton
  contents += disableButton

  reactions += {
    case ButtonClicked(`enableButton`) => enableRule()
    case ButtonClicked(`disableButton`) => disableRule()
  }

  listenTo(enableButton, disableButton)

  private def loadRules(): Unit = {
    client.getRules.safeSwingOnComplete {
      case Failure(exception) => createDialog(contents, exception.getMessage)
      case Success(value) =>
        value.foreach(ruleInfo => rulesInfo += ruleInfo.identifier -> rulePrintify(ruleInfo.rule))
        updateComboBox(rulesInfo.values.toSeq)
        if (rulesInfo.isEmpty) {
          enableButton.enabled = false
          disableButton.enabled = false
          rulesBox.enabled = false
        }
    }
  }

  private def enableRule(): Unit = {
    checkSelection().foreach(id => {
      client.enableRule(id).safeSwingOnCompleteDialog(contents)
    })
  }

  private def disableRule(): Unit = {
    checkSelection().foreach(id => {
      client.disableRule(id).safeSwingOnCompleteDialog(contents)
    })
  }

  private def checkSelection(): Option[String] = {
    val selectedItem = rulesBox.selection.item
    if (selectedItem != null) {
      rulesInfo.find(line => line._2 == selectedItem).map(line => line._1)
    } else {
      None
    }
  }

}

object RuleComponent {
  def apply()(implicit client: GreenHouseClient): RuleComponent = new RuleComponent
}