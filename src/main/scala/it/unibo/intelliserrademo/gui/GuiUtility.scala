package it.unibo.intelliserrademo.gui

import scala.swing.{Button, TextArea}

// scalastyle:off magic.number
object GuiUtility {

  def createButton(name: String): Button = {
    new Button(name)
  }

  def createButtons(names: String*): List[Button] = {
    names.map { name => createButton(name) }.toList
  }

  def createTextArea: TextArea = {
    new TextArea {
      rows = 5;
      lineWrap = true;
      wordWrap = true;
      editable = false
    }
  }

}
