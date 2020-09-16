package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.actuator.Action
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, CharType, DoubleType, IntType, StringType}

object CategoriesAndActions {
  case object AirTemperature extends Category[DoubleType]
  case object SoilMoisture extends Category[DoubleType]
  case object Weather extends Category[StringType]
  case object SunLight extends Category[DoubleType]
  case object DayNight extends Category[StringType]
  case object LightToggle extends Category[BooleanType]
  case object Pressure extends Category[DoubleType]
  case object CharCategory extends Category[CharType]
  case object Water extends Action
  case object Light extends Action
  case object Fan extends Action
  case object Heat extends Action
  case object OpenWindow extends Action
}
