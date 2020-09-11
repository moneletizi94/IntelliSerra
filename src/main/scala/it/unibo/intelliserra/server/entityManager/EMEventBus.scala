package it.unibo.intelliserra.server.entityManager

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification
import it.unibo.intelliserra.core.entity.EntityChannel

/**
 * This object represents the pubSub channel
 * to update subscriber on an entityRemove
 */
object EMEventBus extends EventBus with SubchannelClassification {
  type Event = (String, PublishedOnRemoveEntity)
  type Classifier = String
  type Subscriber = ActorRef

  /**
   * This represents the only topic this channel can understand.
   * Subscribers can subscribe only on this topic.
   * Publishers can publish only on this topic.
   */
  val topic = "topic:removeEntity"

  protected def subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y
    def isSubclass(x: Classifier, y: Classifier): Boolean = x.startsWith(y)
  }

  override def subscribe(subscriber: ActorRef, to: String): Boolean = {
    require(to == this.topic)
    super.subscribe(subscriber, to)
  }

  override protected def classify(event: Event): Classifier = {
    require(event._1 == this.topic)
    event._1
  }

  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    require(event._1 == this.topic)
    subscriber ! event._2
  }

  /**
   * Event published by the event bus. It is a message received by the subscribers (i.e. zoneManager)
   * @param entityChannel it is the entityChannel removed
   */
  case class PublishedOnRemoveEntity(entityChannel: EntityChannel)
}