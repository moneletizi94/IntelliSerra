package it.unibo.intelliserra.common.akka

import akka.actor.ActorPath

object RemotePath {

  def entityManager(greenHouseName: String, serverHost: String, serverPort: Int): ActorPath =
    buildRemotePath(greenHouseName, serverHost, serverPort, "/user/entityManager")

  def server(greenHouseName: String, serverHost: String, serverPort: Int): String =
    buildRemotePath(greenHouseName, serverHost, serverPort, "/user/serverActor").toString

  private def buildRemotePath(greenHouseName: String,
                              serverHost: String,
                              serverPort: Int,
                              actorPath: String): ActorPath = {
    ActorPath.fromString(s"akka.tcp://$greenHouseName@$serverHost:$serverPort$actorPath")
  }
}
