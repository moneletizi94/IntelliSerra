package it.unibo.intelliserra.utils

import java.util.UUID

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{Category, DoubleType, IntType, Measure, NumericType, StringType, ValueType}
import it.unibo.intelliserra.utils.TestUtility.Actions.{Fan, Light, Water}
import it.unibo.intelliserra.utils.TestUtility.Categories
import it.unibo.intelliserra.utils.TestUtility.Categories.{Humidity, Temperature, Weather}

import scala.util.Random

trait Sample[T]{
  def sample : T
}

//abstract class MeasureValueSample[A <: ValueType](category: Category[A]) extends Sample[A]

object Sample{

  /*implicit object WeatherValuesSample extends MeasureValueSample(Weather){
    override def sample: StringType = Generator.oneOf(Samples.WeatherCondition.toList)
  }

  implicit object HumidityValuesSample extends MeasureValueSample(Humidity){
    override def sample: IntType = (Random.nextDouble()*100).toInt
  }

  implicit object TemperatureMeasureSample extends MeasureValueSample(Temperature){
    override def sample: IntType = 20 + Random.nextInt( (30 - 20) + 1 )
  }*/

  implicit object WeatherValuesSample extends Sample[StringType]{
    override def sample: StringType = Generator.oneOf(Samples.WeatherCondition.toList)
  }

  implicit object HumidityValuesSample extends Sample[IntType]{
    override def sample: IntType = (Random.nextDouble()*100).toInt
  }


  implicit object TemperatureMeasureSample extends Sample[IntType]{
    override def sample: IntType = 20 + Random.nextInt( (30 - 20) + 1 )
  }

  implicit object IDSample extends Sample[StringType]{
    override def sample: StringType = UUID.randomUUID().toString
  }


  implicit case object IntRandomSample extends Sample[IntType]{
    override def sample: IntType = Random.nextInt()
  }

  implicit object CategorySample extends Sample[Category[_]]{
    override def sample: Category[_] = Generator.oneOf(Samples.CategoriesSample)
  }

  implicit object ActionSample extends Sample[Action]{
    override def sample: Action = Generator.oneOf(Samples.Actions)
  }

  implicit object MeasuresSample extends Sample[Measure]{
    override def sample: Measure = implicitly[Sample[Category[_]]].sample match {
      case Temperature => MeasureSample(Temperature)(TemperatureMeasureSample).sample
      case Humidity => MeasureSample(Humidity)(HumidityValuesSample).sample
      case Weather => MeasureSample(Weather)(WeatherValuesSample).sample
    }
  }

  case class MeasureSample[A <: ValueType](cat: Category[A])(implicit valueTypeSample : Sample[A]) extends Sample[Measure]{
    override def sample: Measure = Measure(cat)(valueTypeSample.sample)
  }

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

  println(generate[Measure])
  println(generateMore[StringType](10)(IDSample))
  println(generateMore[Measure](10))
  println(generateMore[Measure](10)(MeasureSample(Humidity)(HumidityValuesSample)))

}
