package it.unibo.intelliserra.utils

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{Category, IntType, NumericType, StringType}
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, Light, Water}
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature, Weather}

import scala.util.Random

trait Sample[T]{
  def sample : T
}

object Sample{

  implicit case object WeatherValuesSample extends Sample[StringType]{
    override def sample: StringType = Generator.oneOf(Samples.WeatherCondition.toList)
  }

  case class IntRangeSample(range: Range) extends Sample[IntType]{
    //from scala 2.13 is available Random.between
    override def sample: IntType = Random.nextInt((range.end - range.start) + 1)
  }

  implicit case object IntRandomSample extends Sample[IntType]{
    override def sample: IntType = Random.nextInt()
  }

  implicit case object CategorySample extends Sample[Category[_]]{
    override def sample: Category[_] = Generator.oneOf(Samples.CategoriesSample)
  }

  implicit case object ActionSample extends Sample[Action]{
    override def sample: Action = Generator.oneOf(Samples.Actions)
  }

  /*case class MeasureSample[C : Category[A], A](category: C) extends Sample[Measure]{
    override def sample: Measure = Measure(implicitly[Sample[C]].sample)(implicitly[Sample[A]].sample)
    override def sample: Measure = ???
  }*/
}

object Generator{
  def generate[G : Sample] : G = gen
  def generateOpt[G : Sample] : Option[G] = if(Random.nextBoolean()) Option(gen) else None
  def generateMore[G : Sample](size : Int) : List[G] = List.fill(size)(gen)
  def generateMore[G : Sample](size : Int)(valueTrend : NumericType => NumericType) : List[G] = ??? // TODO: implement this
  def generateStream[G : Sample]: Stream[G] = Stream.continually(gen)
  def oneOf[G](list : List[G]): G = list(Random.nextInt(list.length))

  private def gen[G: Sample] : G = implicitly[Sample[G]].sample
}

object Samples {
  val CategoriesSample = List(Temperature, Humidity, Weather)
  val Actions = List(Water, Fan, Light)
  val WeatherCondition: Seq[StringType] = List("SUNNY", "RAINY", "FOGGY")
}
