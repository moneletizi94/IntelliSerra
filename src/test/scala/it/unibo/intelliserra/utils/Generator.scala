package it.unibo.intelliserra.utils

import java.util.UUID

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{Category, IntType, Measure, NumericType, StringType, ValueType}
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, Light, Water}
import it.unibo.intelliserra.utils.TestUtility.Categories
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature, Weather}

import scala.util.Random

trait Sample[T]{
  def sample : T
}
trait SampleMeasureType[C <: Category[T],T <: ValueType] extends Sample[T]

object Sample{

  implicit case object WeatherValuesSample extends SampleMeasureType[Weather.type , StringType]{
    override def sample: StringType = Generator.oneOf(Samples.WeatherCondition.toList)
  }

  implicit case object IDSample extends Sample[StringType]{
    override def sample: StringType = UUID.randomUUID().toString
  }

  trait IntRangeSample extends Sample[IntType]{
    //from scala 2.13 is available Random.between
    def range: Range
    override def sample: IntType = Random.nextInt((range.end - range.start) + 1)
  }

  implicit case class TemperatureMeasureSample(override val range: Range = 10 to 30) extends SampleMeasureType[Temperature.type , IntType] with IntRangeSample

  implicit case object IntRandomSample extends Sample[IntType]{
    override def sample: IntType = Random.nextInt()
  }

  implicit case object CategorySample extends Sample[Category[_]]{
    override def sample: Category[_] = Generator.oneOf(Samples.CategoriesSample)
  }

  implicit case object ActionSample extends Sample[Action]{
    override def sample: Action = Generator.oneOf(Samples.Actions)
  }

  implicit case object MeasuresSample extends Sample[Measure]{
    import Sample._

    override def sample: Measure = ???/*implicitly[Sample[Category[_]]].sample match {
      case Temperature => MeasureSample(Temperature)
      case Humidity => MeasureSample(Humidity).sample
      case Weather => MeasureSample(Weather)(Sample.WeatherValuesSample).sample
    }*/
  }

  /*implicit case object TemperatureSample extends Sample[Temperature.type ] {
    override def sample: Categories.Temperature.type = ???
  }*/
/*
  case class MeasureSample[A <: ValueType, B : Category[_]](category: B[A])(implicit typeSample : SampleMeasureType[ , A]) extends Sample[Measure]{
    override def sample: Measure = ???Measure(category)(typeSample.sample)
  }*/
/*
  case class MeasureSample[A <: ValueType](category: Category[A])(implicit typeSample : Sample[A]) extends Sample[Measure]{
    override def sample: Measure = Measure(category)(typeSample.sample)
  }*/

}

object Generator{
  def generate[G : Sample] : G = gen
  def generateOpt[G : Sample] : Option[G] = if(Random.nextBoolean()) Option(gen) else None
  def generateMore[G : Sample](size : Int) : List[G] = List.fill(size)(gen)
  def generateMore[G <: NumericType, Sample](size : Int, valueTrend : NumericType => NumericType) : List[G] = {List()} // TODO: implement this
  def oneOf[G](list : List[G]): G = list(Random.nextInt(list.length))

  private def gen[G: Sample] : G = implicitly[Sample[G]].sample
}

object Samples {
  val CategoriesSample = List(Temperature, Humidity, Weather)
  val Actions = List(Water, Fan, Light)
  val WeatherCondition: Seq[StringType] = List("SUNNY", "RAINY", "FOGGY")
}

object Prova extends App{
  import Sample._
  import Generator._
  generate[IntType]
  generateMore[StringType](10)
  println(Generator.generateMore[Measure](10))
  //println(Generator.generateMore[Measure](10)(MeasureSample(Temperature)))
}
