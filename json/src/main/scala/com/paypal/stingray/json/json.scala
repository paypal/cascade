/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.stingray

import scala.util.Try

/**
 * Convenience implicits for reading objects from and writing objects to JSON Strings
 */
package object json {

  // patterns adapted from https://coderwall.com/p/o--apg

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
     * Attempts to convert this String into an object of type `T`
     * @tparam T the type of the object into which the String will be mapped
     * @return a Try of either an object of type `T`, or a Throwable conversion failure
     */
    def fromJson[T : Manifest]: Try[T] =  JsonUtil.fromJson[T](str)
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

    /**
     * Attempts to convert this object into a JSON String
     * @return a Try of either a JSON String, or a Throwable conversion failure
     */
    def toJson: Try[String] = JsonUtil.toJson(marshallMe)
  }

}
