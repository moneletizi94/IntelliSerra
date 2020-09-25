package it.unibo.intelliserrademo.common


import it.unibo.intelliserra.core.perception.NumericType

import scala.util.Random

trait Sample[T]{
  def sample : T
}

object Generator{
  def generate[G : Sample] : G = gen
  def generateOpt[G : Sample] : Option[G] = if(Random.nextBoolean()) Option(gen) else None
  def generateMore[G : Sample](size : Int) : List[G] = List.fill(size)(gen)
  def oneOf[G](list : List[G]): G = list(Random.nextInt(list.length))
  def generateStream[G : Sample]: Stream[G] = Stream.continually(gen)

  private def gen[G: Sample] : G = implicitly[Sample[G]].sample
}

