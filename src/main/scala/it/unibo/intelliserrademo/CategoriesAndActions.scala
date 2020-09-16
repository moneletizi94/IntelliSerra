package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.actuator.{Action, TimedAction}
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, CharType, DoubleType, IntType, StringType}

import scala.concurrent.duration.FiniteDuration

object CategoriesAndActions {
  case object AirTemperature extends Category[DoubleType]
  case object SoilMoisture extends Category[DoubleType]
  case object Weather extends Category[StringType]
  case object SunLight extends Category[DoubleType]
  case object DayNight extends Category[StringType]
  case object LightToggle extends Category[BooleanType]
  case object Pressure extends Category[DoubleType]
  case class Water(override val time: FiniteDuration) extends TimedAction
  case object Light extends Action
  case object Fan extends Action
  case class Heat(override val time : FiniteDuration) extends TimedAction
  case class OpenWindow(override val time : FiniteDuration) extends TimedAction
  case class Notification(message : String) extends Action
}
