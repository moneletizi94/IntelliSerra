package it.unibo.intelliserra.core.sensor

import it.unibo.intelliserra.core.entity.Entity

trait Sensor extends Entity {
  def state: Measure
}
