package it.unibo.intelliserra.core.sensor

import it.unibo.intelliserra.core.entity.SensingCapability
import monix.reactive.Observable
import monix.reactive.subjects.PublishSubject

import scala.concurrent.duration.FiniteDuration

trait MeasureProducer {
  def measures: Observable[Measure]
}

object MeasureProducer {

  def push(): PushProducer = new PushProducer()
  def polling(period: FiniteDuration)(tick: => Measure): PollingProducer = new PollingProducer(period)(tick)

  class PushProducer() extends MeasureProducer {
    private val publishSubject = PublishSubject[Measure]()
    override def measures: Observable[Measure] = publishSubject
    def publishMeasure(measure: Measure): Unit = publishSubject.onNext(measure)
  }

  class PollingProducer(period: FiniteDuration)(tick: => Measure) extends MeasureProducer {
    override def measures: Observable[Measure] = Observable.interval(period).map(_ => tick)
  }
}

trait Sensor {
  def measureStream: Observable[Measure]
  def sensingCapability: SensingCapability
  def init(f: => Unit): Unit
  def onAssignZone(f: String => Unit): Unit
  def onDissociate(f: String => Unit): Unit
}

abstract class BasicPushSensor extends Sensor {
  private val pushBehaviour = MeasureProducer.push()
  override def measureStream: Observable[Measure] = pushBehaviour.measures
  protected def publishMeasure(value: ValueType): Unit = pushBehaviour.publishMeasure(Measure(value, sensingCapability.category))
}

abstract class BasicPollingSensor extends Sensor {
  private val pollingBehaviour = MeasureProducer.polling(pollingTime)(onTick)
  override def measureStream: Observable[Measure] = pollingBehaviour.measures
  def pollingTime: FiniteDuration
  def onTick: Measure
}



/*
trait SensorBehaviour {
  type Stream[T]
  def sensingCapability: SensingCapability
  def measures: Stream[Measure]
}

trait ObservableStream extends SensorBehaviour {
  override type Stream[T] = Observable[T]
}

class A extends SensorBehaviour with ObservableStream {

  override def sensingCapability: SensingCapability = ???
  override def measures: Observable[Measure] = ???

}*/