package it.unibo.intelliserra.examples

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HelloTest extends FlatSpec {

  "Hello world " should "say hello" in {
    val helloWorld = new HelloWorld()
    assert(helloWorld.hello == "Hello World!")
  }
}
