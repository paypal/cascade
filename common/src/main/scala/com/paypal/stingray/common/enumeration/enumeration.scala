package com.paypal.stingray.common

/**
 * Contains a fully type-safe enumeration framework. Takes more setup & code than the built-in [[scala.Enumeration]],
 * but has two distinct advantages:
 *
 * 1. Configurable outcomes when a conversion from a String to Enumeration value fails
 * 2. Non-exhaustive match warnings when one or more Enumeration values are not included in a `match`
 *
 * By comparison, [[scala.Enumeration]] will allow a `match` over as few as one Enumeration type, which runs the risk
 * of a [[scala.MatchError]]. This will alert at compile time as to the possibility of a `match` miss.
 *
 * As of Jackson 2.x, a method exists to directly serialize [[scala.Enumeration]], which involves writing less
 * up-front boilerplate. One distinct downside, however, is the need for a special Jackson annotation on case classes
 * that use Enumerations in this way. The annotation must be applied directly to each case class member that is an
 * Enumeration. In this way, serializing [[scala.Enumeration]] in Jackson involves substantially more boilerplate
 * effort than the custom Enumeration presented here. See
 * https://github.com/FasterXML/jackson-module-scala/wiki/Enumeration for more information.
 *
 * For these reasons, our custom Enumeration type should be preferred instead of [[scala.Enumeration]] or
 * [[java.util.Enumeration]]. See the examples subproject for a sample implementation.
 */

package object enumeration {

  /**
   * Our custom Enumeration type.
   */
  trait Enumeration extends Serializable {
    override def toString: String = stringVal

    /** Forces stable serialization by requiring a String representation */
    def stringVal: String

    /** Provides a quick interface to compare Enumeration values */
    def matches(s: String): Boolean = s.toLowerCase.equals(stringVal)
  }

  /**
   * Exception type for failed String-to-Enumeration reading
   * @param unknownValue the supposed stringVal of the failed Enumeration read
   */
  class EnumerationException(unknownValue: String) extends Exception("Unknown enumeration value " + unknownValue)

  /**
   * Creates an [[com.paypal.stingray.common.enumeration.EnumReader]]
   * @param reader the method to convert a String to an Enumeration
   * @tparam T the Enumeration type
   * @return an EnumReader that knows how to read a String to an Enumeration
   */
  def enumReader[T <: Enumeration](reader: String => Option[T]): EnumReader[T] = new EnumReader[T] {
    override def read(s: String): Option[T] = reader(s)
  }

  /**
   * Creates an [[com.paypal.stingray.common.enumeration.EnumReader]] that converts a String to an Enumeration
   * if the lowercase version of that String matches the lowercase of the Enumeration's stringVal
   * @param values the Enumeration values that are candidates to convert
   * @tparam T the Enumeration type
   * @return an EnumReader that has the aforementioned properties
   */
  def lowerEnumReader[T <: Enumeration](values: T*): EnumReader[T] = enumReader { s: String =>
    values.find { t: T =>
      t.stringVal.toLowerCase == s.toLowerCase
    }
  }

  /**
   * Creates an [[com.paypal.stingray.common.enumeration.EnumReader]] that converts a String to an Enumeration
   * if the uppercase version of that String matches the uppercase of the Enumeration's stringVal
   * @param values the Enumeration values that are candidates to convert
   * @tparam T the Enumeration type
   * @return an EnumReader that has the aforementioned properties
   */
  def upperEnumReader[T <: Enumeration](values: T*): EnumReader[T] = enumReader { s: String =>
    values.find { t: T =>
      t.stringVal.toUpperCase == s.toUpperCase
    }
  }

  /**
   * Implicit wrapper for Strings to perform Enumeration reading,
   * using an implicit [[com.paypal.stingray.common.enumeration.EnumReader]]
   * @param value the wrapped String
   */
  implicit class RichStringEnumReader(value: String) {

    /**
     * Reads the wrapped String and, if it corresponds to an Enumeration value, returns that value; otherwise, None
     * @param reader implicitly, the [[com.paypal.stingray.common.enumeration.EnumReader]] to use for reading
     * @tparam T the Enumeration type
     * @return optionally, an instance of the Enumeration type
     */
    def readEnum[T <: Enumeration](implicit reader: EnumReader[T]): Option[T] = reader.read(value)

    /**
     * Reads the wrapped String and fits it to an Enumeration value, or throws
     * @param reader implicitly, the [[com.paypal.stingray.common.enumeration.EnumReader]] to use for reading
     * @tparam T the Enumeration type
     * @return an instance of the Enumeration type
     * @throws EnumerationException if no mapping to an Enumeration type can be found
     */
    @throws[EnumerationException]
    def toEnum[T <: Enumeration](implicit reader: EnumReader[T]): T = reader.withName(value)
  }

}
