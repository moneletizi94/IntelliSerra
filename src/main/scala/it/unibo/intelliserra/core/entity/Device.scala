package it.unibo.intelliserra.core.entity

/**
 * This trait represents a device that can be added to the system: Sensor or Actuator.
 */
trait Device {

  /**
   * It represent a device name or identifier
   * @return device's name
   */
  def identifier : String

  /**
   * Represent the capabilities of this particular device.
   * If will be created a sensor, SensingCapability, ActingCapability otherwise.
   *
   * @return type of device's capability.
   */
  def capability : Capability
}