package it.unibo.intelliserra.server.entityManager

import akka.actor.ActorRef
import it.unibo.intelliserra.core.entity.{Capability, Device}

// TODO: scaladoc 
case class RegisteredDevice(override val identifier: String, override val capability: Capability) extends Device
case class DeviceChannel(device: Device, channel: ActorRef)