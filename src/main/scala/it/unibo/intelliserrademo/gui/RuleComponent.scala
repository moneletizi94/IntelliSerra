package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserra.core.rule.RuleInfo
import javax.swing.BorderFactory

import scala.concurrent.ExecutionContext.Implicits.global
import it.unibo.intelliserrademo.gui.util.SwingFuture._

import scala.swing.{Button, ComboBox, Dialog, Dimension, FlowPanel}
import scala.util.{Failure, Success}

// scalastyle:off magic.number
private[gui] class RuleComponent(implicit client: GreenHouseClient) extends FlowPanel {

  private var rulesInfo: List[RuleInfo] = List()
  private val rulesBox = new ComboBox(rulesInfo.map(_.identifier))
  private val enableButton = new Button("Enable rule")
  private val disableButton = new Button("Disable rule")

  loadRules()

  rulesBox.peer.setBorder(BorderFactory.createTitledBorder("Rules"))
  rulesBox.preferredSize = new Dimension(150,50)

  contents += rulesBox
  contents += enableButton //TODO add reactions
  contents += disableButton //TODO add reactions

  def loadRules(): Unit = {
    client.getRules.safeSwingOnComplete {
      case Failure(exception) => Dialog.showMessage(contents.head, exception.getMessage)
      case Success(value) =>
        rulesInfo = value
        rulesBox.peer.setModel(ComboBox.newConstantModel(rulesInfo.map(_.identifier).toSeq))
    }

  }
}

object RuleComponent {
  def apply()(implicit client: GreenHouseClient): RuleComponent = new RuleComponent
}