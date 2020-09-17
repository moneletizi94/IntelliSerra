package it.unibo.intelliserrademo.common

import it.unibo.intelliserra.core.actuator.{Action, TimedAction, ToggledAction}
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, CharType, DoubleType, IntType, StringType}
import it.unibo.intelliserra.core.actuator.{Action, TimedAction}
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, DoubleType, StringType}

import scala.concurrent.duration.FiniteDuration

object CategoriesAndActions {
  case object Humidity extends Category[DoubleType]
  case object AirTemperature extends Category[DoubleType]
  case object SoilMoisture extends Category[DoubleType]
  case object Weather extends Category[StringType]
  case object SunLight extends Category[DoubleType]
  case object DayNight extends Category[StringType]
  case object LightToggle extends Category[BooleanType]
  case object Pressure extends Category[DoubleType]
  case class Water(override val time: FiniteDuration) extends TimedAction
  case object Light extends Action
  case class Fan(override val time : FiniteDuration) extends TimedAction
  case class Heat(override val time : FiniteDuration) extends TimedAction
  case class OpenWindow(override val time : FiniteDuration) extends TimedAction
  case class Notification(message : String) extends Action
  case class Dehumidifies(override val switchStatus: Boolean) extends ToggledAction
}
