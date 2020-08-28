package it.unibo.intelliserra.core.sensor

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


