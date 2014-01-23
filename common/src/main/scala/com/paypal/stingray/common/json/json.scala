package com.paypal.stingray.common

import scala.util.Try

/**
 * Convenience implicits for reading objects from and writing objects to JSON Strings
 */
package object json {

  // patterns adapted from https://coderwall.com/p/o--apg
  // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that

  /**
   * Implicit conversions from a JSON String to a variety of object types
   *
   * {{{
   *   import com.paypal.stingray.common.json._
   *   """{"key":"value"}""".toMap
   * }}}
   *
   * @param str this String
   */
  implicit class Unmarshallable(str: String) {

    /**
     * Attempts to convert this String into a Map of objects
     * @return a Try of either a Map of keys to objects, or a Throwable
     */
    def toMap: Try[Map[String, Any]] = JsonUtil.fromJsonToMap(str)

    /**
     * Attempts to convert this String into a Map of objects of type `T`
     * @tparam T the type of the value object in this map
     * @return a Try of either a Map of keys to objects of type `T`, or a Throwable
     */
    def toMapOf[T : Manifest](): Try[Map[String, T]] = JsonUtil.fromJsonToMap[T](str)

    /**
     * Attempts to convert this String into an object of type `T`
     * @tparam T the type of the object into which the String will be mapped
     * @return a Try of either an object of type `T`, or a Throwable
     */
    def fromJson[T : Manifest](): Try[T] =  JsonUtil.fromJson[T](str)
  }

  /**
   * Implicit conversions from a given object of type `T` to a JSON String
   *
   * {{{
   *   import com.paypal.stingray.common.json._
   *   case class AnObject(v1: String, v2: Long, v3: List[String])
   *   val a = AnObject("value", 5L, List("hi", "there")
   *   a.toJson
   * }}}
   *
   * @param marshallMe this object
   * @tparam T the type of this object
   */
  implicit class Marshallable[T](marshallMe: T) {
    def toJson: Try[String] = JsonUtil.toJson(marshallMe)
  }

}
