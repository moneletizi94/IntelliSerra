package it.unibo.intelliserra.utils

import it.unibo.intelliserra.core.sensor.{Category, IntType, StringType}

trait TestCategory {
  case object Temperature extends Category[IntType]
  case object Humidity extends Category[IntType]
  case object Weather extends Category[StringType]
}
