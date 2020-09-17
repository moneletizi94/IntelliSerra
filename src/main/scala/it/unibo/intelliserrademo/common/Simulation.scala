package it.unibo.intelliserrademo.common

import it.unibo.intelliserra.core.sensor.{DoubleType, StringType}

import scala.util.Random

object Simulation {

  val sin: (Double, Double) => Int => Double =
    (stdValue, delta) => iteration => stdValue + Math.sin(iteration / delta) * delta

  case class DoubleSinSample(stdValue: Double, delta: Double) extends Sample[DoubleType] {
    private var iteration: Int = 0
    private val generationFunction = sin(stdValue, delta)
    override def sample: DoubleType = { iteration += 1; generationFunction(iteration) }
  }

  case class StringSample(probabilityMap: (String, Double)*) extends Sample[StringType] {
    private val random = RandomWithProbability(probabilityMap.toMap)
    override def sample: StringType = random.nextItem
  }

  case class RandomWithProbability[T](items: Map[T, Double]) {
    require(items.values.sum == 1.0, "The sum of total probability must be 1")

    def nextItem: T = {
      val random = Random.nextDouble()
      val absoluteProbabilityList = items.foldRight(List[(T, Double)]()) {
        case ((item, relativeProp), acc) => acc :+ (item ->  acc.lastOption.fold(relativeProp)(_._2 + relativeProp))
      }
      absoluteProbabilityList.find(kv => kv._2 >= random).map(_._1).get
    }

    def asStream: Stream[T] = Stream.continually(nextItem)
  }
}
