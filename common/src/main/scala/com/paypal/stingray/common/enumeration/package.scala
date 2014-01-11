package com.paypal.stingray.common

import net.liftweb.json.JsonAST._
import com.paypal.stingray.common.validation._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.enumeration
 *
 * User: aaron
 * Date: 12/17/12
 * Time: 4:46 PM
 */

package object enumeration {

  trait Enumeration extends Serializable {
    override def toString: String = stringVal
    def stringVal: String
    def matches(s: String): Boolean = s.toLowerCase.equals(stringVal)
  }

  class EnumerationException(s: String) extends Exception("Unknown enumeration value " + s)

  /**
   * shortcut to create an EnumReader
   * @param reader the method to convert a string to your enum
   * @tparam T the enum type to convert
   * @return an EnumReader that knows how to read a string into your enum
   */
  def enumReader[T <: Enumeration](reader: String => Option[T]): EnumReader[T] = new EnumReader[T] {
    override def read(s: String): Option[T] = reader(s)
  }

  /**
   * creates an EnumReader that converts a string into an enum if the lowercase
   * version of that string matches the lowercase of the enum
   * @param values the enumeration values that are candidates to convert
   * @tparam T the enumeration type
   * @return an EnumReader that has the aforementioned properties
   */
  def lowerEnumReader[T <: Enumeration](values: T*): EnumReader[T] = enumReader { s: String =>
    values.find { t: T =>
      t.stringVal.toLowerCase == s.toLowerCase
    }
  }

  /**
   * creates an EnumReader that converts a string into an enum if the uppercase
   * version of that string matches the uppercase of the enum
   * @param values the enumeration values that are candidates to convert
   * @tparam T the enumeration type
   * @return an EnumReader that has the aforementioned properties
   */
  def upperEnumReader[T <: Enumeration](values: T*): EnumReader[T] = enumReader { s: String =>
    values.find { t: T =>
      t.stringVal.toUpperCase == s.toUpperCase
    }
  }

  implicit class RichStringEnumReader(value: String) {
    def readEnum[T <: Enumeration](implicit reader: EnumReader[T]): Option[T] = reader.read(value)
    def toEnum[T <: Enumeration](implicit reader: EnumReader[T]): T = reader.withName(value)
  }

  implicit def enumerationJSON[T <: Enumeration](implicit reader: EnumReader[T], m: Manifest[T]) = new JSON[T] {
    override def write(value: T): JValue = JString(value.stringVal)
    override def read(json: JValue): Result[T] = json match {
      case JString(s) => (validating(reader.withName(s)).mapFailure { _ =>
        UncategorizedError(s, "Invalid %s: %s".format(m.runtimeClass.getSimpleName, s), Nil)
      }).toValidationNel
      case j => UnexpectedJSONError(j, classOf[JString]).failNel
    }
  }

}
