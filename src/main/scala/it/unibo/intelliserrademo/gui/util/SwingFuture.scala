package it.unibo.intelliserrademo.gui.util

import javax.swing.SwingUtilities

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.swing.{Component, Dialog}
import scala.util.{Failure, Success, Try}

object SwingFuture {

  implicit class SwingFuture[T <: Any](future: Future[T])(implicit val context: ExecutionContext) {
    def safeSwingOnComplete(logic: Try[T] => Unit): Unit = future.onComplete( e => SwingUtilities.invokeLater(() => logic(e)))

    def safeSwingOnCompleteValue(logic: String => Unit): Unit = future.onComplete {
        case Failure(exception) => SwingUtilities.invokeLater(() => logic(exception.getMessage))
        case Success(value) => SwingUtilities.invokeLater(() => logic(value.toString))
      }

    def safeSwingOnCompleteDialog(contents: mutable.Buffer[Component]): Unit = future.onComplete {
      case Failure(exception) => SwingUtilities.invokeLater(() => Dialog.showMessage(contents.head, exception.getMessage))
      case Success(value) => SwingUtilities.invokeLater(() => Dialog.showMessage(contents.head, value.toString))
    }

  }

}
