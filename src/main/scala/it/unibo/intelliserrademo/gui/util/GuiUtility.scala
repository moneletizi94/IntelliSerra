package it.unibo.intelliserrademo.gui.util

import scala.swing.{Button, TextArea}

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

}
