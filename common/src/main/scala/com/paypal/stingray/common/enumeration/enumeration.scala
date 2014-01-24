package com.paypal.stingray.common

import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, SerializerProvider, JsonSerializer}
import com.fasterxml.jackson.core.{JsonParser, JsonGenerator}
import com.paypal.stingray.common.enumeration.EnumReader
import com.fasterxml.jackson.databind.annotation.{JsonSerialize, JsonDeserialize}

/**
 * Contains our custom Enumeration type, as well as methods to help interact with that type.
 */

package object enumeration {

  /**
   * Our custom Enumeration type. Prefer this instead of [[scala.Enumeration]] or [[java.util.Enumeration]].
   * See the examples subproject for an implementation.
   *
   * Our Enumeration will raise non-exhaustive match warnings when used in pattern matches, unlike
   * [[scala.Enumeration]] which lets you partially match (and in turn run the risk of a [[scala.MatchError]]).
   *
   * The price for this match safety is a fair amount of boilerplate for each implementation. Again, see the
   * examples subproject.
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
    def toEnum[T <: Enumeration](implicit reader: EnumReader[T]): T = reader.withName(value)
  }

}
