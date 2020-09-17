package it.unibo.intelliserrademo.gui

import it.unibo.intelliserra.client.core.GreenHouseClient
import it.unibo.intelliserrademo.gui.util.SwingFuture._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.event.ButtonClicked
import scala.swing.{Button, Dialog, FlowPanel}
import scala.util.{Failure, Success}
import it.unibo.intelliserrademo.gui.util.GuiUtility._

private[gui] class EntityComponent(implicit client: GreenHouseClient) extends FlowPanel {

  private val removeEntityButton = new Button("Remove entity")
  private val dissociateEntityButton = new Button("Dissociate entity")

  contents += removeEntityButton
  contents += dissociateEntityButton

  listenTo(removeEntityButton, dissociateEntityButton)

  reactions += {
    case ButtonClicked(`removeEntityButton`) => removeEntity()
    case ButtonClicked(`dissociateEntityButton`) => dissociateEntity()
  }

  private def removeEntity(): Unit = {
    val entityToRemove = Dialog.showInput(contents.head, "Entity name:", initial = "")
    entityToRemove.foreach(entityValue => {
      val entity = entityValue.replaceAll(" ", "")
      if(entity.length > 0) {
        client.removeEntity(entity)
          .safeSwingOnComplete {
            case Failure(exception) => createDialog(contents, exception.getMessage)
            case Success(_) => createDialog(contents, "Entity deleted")
          }
      } else { createDialog(contents, "Invalid parameter")}
    })
  }
  private def dissociateEntity(): Unit = {
    val entityToDissociate = Dialog.showInput(contents.head, "Entity name:", initial = "")
    entityToDissociate.foreach(entityValue => {
      val entity = entityValue.replaceAll(" ", "")
      if(entity.length > 0) {
        client.dissociateEntity(entity)
         .safeSwingOnComplete {
            case Failure(exception) => createDialog(contents, exception.getMessage)
            case Success(_) => createDialog(contents, "Entity dissociated")
          }
      } else { createDialog(contents, "Invalid parameter")}
    })
  }
}


object EntityComponent {
  def apply()(implicit client: GreenHouseClient): EntityComponent = new EntityComponent
}
