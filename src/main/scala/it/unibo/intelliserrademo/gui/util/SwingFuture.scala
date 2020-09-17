package it.unibo.intelliserrademo.gui.util

import javax.swing.SwingUtilities

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object SwingFuture {

  implicit class SwingFuture[T <: Any](future: Future[T])(implicit val context: ExecutionContext) {
    def safeSwingOnComplete(logic: Try[T] => Unit): Unit = future.onComplete( e => SwingUtilities.invokeLater(() => logic(e)))
    def safeSwingOnCompleteValue(logic: String => Unit): Unit = future.onComplete {
        case Failure(exception) => SwingUtilities.invokeLater(() => logic(exception.getMessage))
        case Success(value) => SwingUtilities.invokeLater(() => logic(value.toString))
      }
  }

}
