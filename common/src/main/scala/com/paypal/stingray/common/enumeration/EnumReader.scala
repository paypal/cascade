package com.paypal.stingray.common.enumeration

/**
 * Base trait for reading Strings into our [[com.paypal.stingray.common.enumeration.Enumeration]].
 */
trait EnumReader[T] {

  /**
   * Attempt to read a String into an [[com.paypal.stingray.common.enumeration.Enumeration]] subtype
   * @param s the String to read
   * @return optionally, an Enumeration type, or None if no match is made
   */
  def read(s: String): Option[T]

  /**
   * Reads a String into an [[com.paypal.stingray.common.enumeration.Enumeration]] subtype
   * @param s the String to read
   * @return an Enumeration subtype
   * @throws EnumerationException if no match is made
   */
  @throws[EnumerationException]
  def withName(s: String): T = read(s) match {
    case Some(v) => v
    case None => throw new EnumerationException(s)
  }

}
