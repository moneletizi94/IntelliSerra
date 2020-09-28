package it.unibo.intelliserra.common.akka

import akka.actor.ActorPath

object RemotePath {

  /**
   * Create a URI to remote server actor endpoint
   * @param greenHouseName  the name of greenhouse
   * @param serverHost      the hostname where the server is running
   * @param serverPort      the port where the server is running
   * @return
   */
  def server(greenHouseName: String, serverHost: String, serverPort: Int): String =
    buildRemotePath(greenHouseName, serverHost, serverPort, "/user/serverActor").toString


  /**
   * Create a URI of remote EntityManager actor endpoint
   * @param greenHouseName  the name of greenhouse
   * @param serverHost      the hostname where the server is running
   * @param serverPort      the port where the server is running
   * @return
   */
  def entityManager(greenHouseName: String, serverHost: String, serverPort: Int): String =
    buildRemotePath(greenHouseName, serverHost, serverPort, "/user/entityManager").toString
  
  private def buildRemotePath(greenHouseName: String,
                              serverHost: String,
                              serverPort: Int,
                              actorPath: String): ActorPath = {
    ActorPath.fromString(s"akka.tcp://$greenHouseName@$serverHost:$serverPort$actorPath")
  }
}
