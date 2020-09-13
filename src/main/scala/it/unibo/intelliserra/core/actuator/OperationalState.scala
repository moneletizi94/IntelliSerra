package it.unibo.intelliserra.core.actuator

sealed trait OperationalState{
  def isDoing() : Boolean

}

final case class DoingActions(actions : List[Action]) extends OperationalState {
  override def isDoing(): Boolean = true
}
case object Idle extends OperationalState {
  override def isDoing(): Boolean = false
}