package it.unibo.intelliserra.core
//TODO scaladoc
package object sensor {

  implicit def int2IntType(value: Int) : IntType =  IntType(value)
  implicit def long2DoubleType(value: Long) : DoubleType =  DoubleType(value)
  implicit def double2DoubleType(value: Double) : DoubleType =  DoubleType(value)
  implicit def string2StringType(value: String) : StringType =  StringType(value)
  implicit def char2CharType(value: Char) : CharType =  CharType(value)
  implicit def bool2BoolType(value: Boolean) : BooleanType =  BooleanType(value)

}

