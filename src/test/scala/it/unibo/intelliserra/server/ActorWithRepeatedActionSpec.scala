package it.unibo.intelliserra.server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import it.unibo.intelliserra.utils.TestUtility
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatestplus.junit.JUnitRunner
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

@RunWith(classOf[JUnitRunner])
class ActorWithRepeatedActionSpec extends TestKit(ActorSystem("MyTest")) with TestUtility
  with ImplicitSender
  with Matchers
  with WordSpecLike
  with BeforeAndAfter
  with BeforeAndAfterAll {

  "An empty test" must {
    "empty" in {
      print("A")
    }
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

}
