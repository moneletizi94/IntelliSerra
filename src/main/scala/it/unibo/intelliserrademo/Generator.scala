package it.unibo.intelliserrademo


import it.unibo.intelliserra.core.sensor.NumericType

import scala.util.Random

trait Sample[T]{
  def sample : T
}

object Generator{
  def generate[G : Sample] : G = gen
  def generateOpt[G : Sample] : Option[G] = if(Random.nextBoolean()) Option(gen) else None
  def generateMore[G : Sample](size : Int) : List[G] = List.fill(size)(gen)
  def generateMore[G <: NumericType, Sample](size : Int, valueTrend : NumericType => NumericType) : List[G] = {List()} // TODO: implement this
  def oneOf[G](list : List[G]): G = list(Random.nextInt(list.length))
  def generateStream[G : Sample]: Stream[G] = Stream.continually(gen)

  private def gen[G: Sample] : G = implicitly[Sample[G]].sample
}

