package it.unibo.intelliserrademo

import it.unibo.intelliserra.core.actuator.{Action, Actuator, Idle, OperationalState, TimedTask}
import it.unibo.intelliserra.core.actuator.Actuator.ActionHandler
import it.unibo.intelliserra.core.entity.Capability
import it.unibo.intelliserra.core.entity.Capability.ActingCapability
import it.unibo.intelliserrademo.CategoriesAndActions.Water

import scala.concurrent.duration._
class Actuators {

  case class WaterActuator(override val identifier: String) extends Actuator {
    override def capability: Capability.ActingCapability = ActingCapability(Set(Water))

    override def actionHandler: ActionHandler = {
      case (_, Water) => println("start doing water for 10 seconds"); TimedTask(Water, 10 seconds)(_ => println("water finished"))
    }
    /**
     * The unique identifier associated to a Device
     */
    override def onInit(): Unit = ???

    override def onAssociateZone(zoneName: String): Unit = println(s"WaterActuator associated to zone $zoneName")

    override def onDissociateZone(zoneName: String): Unit = println(s"WaterActuator dissociated to zone $zoneName")
  }



}
