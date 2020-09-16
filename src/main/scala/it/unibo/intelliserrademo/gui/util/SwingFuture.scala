package it.unibo.intelliserrademo.gui.util

import javax.swing.SwingUtilities

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object SwingFuture {

  implicit class SwingFuture[E <: Any](future: Future[E])(implicit val context: ExecutionContext) {

    def safeSwingOnComplete(logic: Try[E] => Unit) = future.onComplete(e => SwingUtilities.invokeLater(() => logic(e)))
  }

}
