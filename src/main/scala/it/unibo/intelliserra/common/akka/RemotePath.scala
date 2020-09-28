package it.unibo.intelliserra.common.akka

import akka.actor.ActorPath

object RemotePath {

  // TODO: documentation
  def entityManager(greenHouseName: String, serverHost: String, serverPort: Int): String =
    buildRemotePath(greenHouseName, serverHost, serverPort, "/user/entityManager").toString

  // TODO: documentation
  def server(greenHouseName: String, serverHost: String, serverPort: Int): String =
    buildRemotePath(greenHouseName, serverHost, serverPort, "/user/serverActor").toString

  private def buildRemotePath(greenHouseName: String,
                              serverHost: String,
                              serverPort: Int,
                              actorPath: String): ActorPath = {
    ActorPath.fromString(s"akka.tcp://$greenHouseName@$serverHost:$serverPort$actorPath")
  }
}
