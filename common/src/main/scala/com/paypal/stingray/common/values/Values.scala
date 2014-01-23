package com.paypal.stingray.common.values

import com.paypal.stingray.common.enumeration._
import scala.language.higherKinds
import scala.util.Try
import com.paypal.stingray.common.json.JsonUtil

/**
 * Base trait for creating Values producers, e.g. [[com.paypal.stingray.common.values.DynamicValues]] and
 * [[com.paypal.stingray.common.values.StaticValues]].
 *
 * Values producers that require asynchronous calls should implement their own async-safe wrappers that tie into
 * this interface. See [[com.paypal.stingray.common.values.DynamicValues]] for an example, or simply extend that.
 *
 * Expected return values can be treated as a bare String, primitives such as Int or Bool,
 * a value of a given Enumeration, a comma-separated List, or parsed JSON.
 */
trait Values {

  /**
   * The basic abstract method to implement for a Values producer, returns an optional String
   * @param key the key to retrieve
   * @return an optional String value for the given `key`
   */
  def get(key: String): Option[String]

  /**
   * Retrieves an optional value and parses it as though it were a comma-separated list, int a List[String]
   * @param key the key to retrieve
   * @return an optional comma-separated list
   */
  def getSimpleList(key: String): Option[List[String]] = {
    get(key).map { value =>
      value.split(",").toList
    }
  }

  /**
   * Retrieves an optional value and attempts to parse it as an Int
   * @param key the key to retrieve
   * @return an optional Int value
   */
  def getInt(key: String): Option[Int] = {
    get(key).flatMap { value =>
      Try(value.toInt).toOption
    }
  }

  /**
   * Retrieves an optional value and attempts to parse it as a Long
   * @param key the key to retrieve
   * @return an optional Long value
   */
  def getLong(key: String): Option[Long] = {
    get(key).flatMap { value =>
      Try(value.toLong).toOption
    }
  }

  /**
   * Retrieves an optional value and attempts to parse it as an [[com.paypal.stingray.common.enumeration.Enumeration]]
   * @param key the key to retrieve
   * @tparam T the concrete type of the Enumeration
   * @return an optional Enumeration value
   */
  def getEnum[T <: Enumeration : EnumReader](key: String): Option[T] = {
    get(key).flatMap { value =>
      value.readEnum[T]
    }
  }

  /**
   * Retrieves an optional value and attempts to parse it as a Boolean
   * @param key the key to retrieve
   * @return an optional Boolean value
   */
  def getBool(key: String): Option[Boolean] = {
    get(key).flatMap { value =>
      Try(value.toBoolean).toOption
    }
  }

  /**
   * Retrieves an optional value and attempts to parse it as a JSON representation of an object of type `T`,
   * using the Jackson parser implementation in [[com.paypal.stingray.common.json.JsonUtil]]
   * @param key the key to retrieve
   * @tparam T the object type to be used for JSON parsing
   * @return an optional object of type `T`
   */
  def getJson[T : Manifest](key: String): Option[T] = {
    get(key).flatMap { value =>
      JsonUtil.fromJson[T](value).toOption
    }
  }
}
