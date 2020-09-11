package it.unibo.intelliserra.core.prolog

import alice.tuprolog.{Struct, Term}
import it.unibo.intelliserra.core.sensor.{BooleanType, Category, CharType, DoubleType, IntType, Measure, StringType, ValueType}
import it.unibo.intelliserra.core.state.State

class StateToProlog extends PrologRepresentation[State] {

  override def toTerm(data: State): Struct = {
    Struct.list(data.perceptions.map(measureToTerm):_*)
  }

  private def measureToTerm(measure: Measure): Term = {
    val atomValue = valueToAtom(measure.value)
    val categoryName = extractCategoryName(measure.category)
    Term.createTerm(s"measure($atomValue, $categoryName)")
  }

  private def extractCategoryName(category: Category[ValueType]): String =
    category.getClass.getSimpleName.split('$').head.toLowerCase

  private def valueToAtom(measureValue: ValueType): String = measureValue match {
    case StringType(value) => s"string($value)"
    case IntType(value) => value.toString
    case DoubleType(value) => value.toString
    case CharType(value) => value.toInt.toString
    case BooleanType(value) => if (value) "1" else "0"
  }
}
