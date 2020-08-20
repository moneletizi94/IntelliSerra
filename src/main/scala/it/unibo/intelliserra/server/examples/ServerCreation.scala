package it.unibo.intelliserra.server.examples

import it.unibo.intelliserra.server.core.GreenHouseServer

object ServerCreation extends App {
  val NAME = "serradipomodori1"
  val greenHouseServer = GreenHouseServer(NAME)

  greenHouseServer.start()
  greenHouseServer.terminate()
}
