package com.paypal.stingray.common.translatable.primitives

import org.codehaus.jackson.annotate.JsonIgnore

abstract class AbstractString (val value: String) extends CharSequence with OpaqueID {

  def this() = this(null)

  override def charAt(index: Int): Char = value.charAt(index)

  override def length: Int = value.length

  @JsonIgnore
  def isEmpty: Boolean = value.isEmpty

  override def subSequence(start: Int, end: Int): CharSequence = value.subSequence(start, end)

  override def hashCode: Int = value.hashCode

  override def equals(obj: Any): Boolean = obj match {
    case o: AbstractString if getClass == o.getClass => value == o.toString
    case _ => false
  }

  def compareTo(other: AbstractString): Int = value.compareTo(other.value)

  override def toString: String = value

  override def getUnderlyingPrimitive: AnyRef = value
}

object AbstractString extends AbstractStringCommon

trait AbstractStringCommon {

  @throws(classOf[InvalidStringException])
  def assertValid(input: String, regex: String, regexDesc: String, minlength: Int, maxlength: Int) {
    if(Option(input).isEmpty) {
      throw new InvalidStringException("cannot be null")
    }

    if (!input.matches(regex)) {
      throw new InvalidStringException(regexDesc)
    }

    if (input.length < minlength) {
      throw new InvalidStringException("must be at least " + minlength + " characters")
    }

    if (input.length > maxlength) {
      throw new InvalidStringException("must be no more than " + maxlength + " characters")
    }
  }

}

class InvalidStringException(msg: String) extends Exception(msg)
