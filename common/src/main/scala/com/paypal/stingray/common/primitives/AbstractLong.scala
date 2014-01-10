package com.paypal.stingray.common.translatable.primitives

@SerialVersionUID(1l)
abstract class AbstractLong(var value: Long) extends OpaqueID {

  def this() = this(0)

  def longValue: Long = value

  override def toString: String = String.valueOf(value)

  override def hashCode: Int = (value ^ (value >>> 32)).asInstanceOf[Int]

  override def equals(obj: Any): Boolean = obj match {
    case o: AbstractLong if getClass == o.getClass => value == o.longValue
    case _ => false
  }

  override def getUnderlyingPrimitive: AnyRef = java.lang.Long.valueOf(value)

  protected def setValue(l: Long) {
    if (value != 0) {
      throw new NullPointerException("Not modifiable")
    }
    value = l
  }

}

object AbstractLong extends AbstractLongCommon

trait AbstractLongCommon {

  def isValid(l: AbstractLong): Boolean = l != null && l.longValue > 0

  def asLongOrNull(l: AbstractLong): java.lang.Long = if (l == null) null else new java.lang.Long(l.longValue)

}
