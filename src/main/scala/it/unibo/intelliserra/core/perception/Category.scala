package it.unibo.intelliserra.core.perception

/**
 * Wraps the type of data a sensor can sense
 * @tparam Value, type of the value that a sensor can sense
 */
trait Category[+Value <: ValueType]
