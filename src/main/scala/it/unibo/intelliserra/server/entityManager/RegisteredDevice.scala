package it.unibo.intelliserra.server.entityManager

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.{Capability, Device}

/**
  Is a base implementation of Device trait that represents a registered device in the system
 */
final case class RegisteredDevice(override val identifier: String, override val capability: Capability) extends Device

/**
  Represents a direct communication channel to communicate with the actor associated to the device.
  In fact, it is composed of the device and the ActorRef associated to the actor.
 */
final case class DeviceChannel(device: Device, channel: ActorRef)