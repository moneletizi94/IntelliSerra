package it.unibo.intelliserrademo.gui.util

import scala.collection.mutable
import scala.swing.{ComboBox, Component, Dialog, TextArea}

// scalastyle:off magic.number
object GuiUtility {

  def createTextArea: TextArea = {
    new TextArea {
      rows = 5;
      lineWrap = true;
      wordWrap = true;
      editable = false
    }
  }

  def createDialog(contents: mutable.Buffer[Component], message: String): Unit = {
    Dialog.showMessage(contents.head, message)
  }

  def updateComboBox[A](items: Seq[A])(implicit comboBox: ComboBox[A]): Unit = {
    comboBox.peer.setModel(ComboBox.newConstantModel(items))
  }

}
