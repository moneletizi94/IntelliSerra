package it.unibo.intelliserra.core.prolog

import alice.tuprolog.Term

/**
 * This trait represents a prolog converter from type T to prolog Term.
 *
 * @tparam T, the type of value to be converted.
 */
trait PrologRepresentation[T] {

  /**
   * Apply the conversion from T to prolog Term.
   *
   * @param data, the value to be converted.
   * @return a prolog term.
   */
  def toTerm(data: T): Term
}