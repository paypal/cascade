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
package com.paypal.cascade

import scala.util.Try

/**
 * Convenience implicits for reading objects from and writing objects to JSON Strings
 */
package object json {

  // patterns adapted from https://coderwall.com/p/o--apg

  /**
   * Implicit conversions from a JSON String to a variety of object types.
   *
   * {{{
   *   import com.paypal.cascade.json._
   *   """{"key":"value"}""".toMap
   * }}}
   *
   * @param str this String
   */
  implicit class Unmarshallable(str: String) {

    /**
     * Convert a JSON string to a `T`, where `T` is some context bound type.
     *
     * @tparam T a context bound type
     * @return a [[scala.util.Try]] that is either the object of type `T`, or one of
     *         [[java.io.IOException]], [[com.fasterxml.jackson.core.JsonParseException]],
     *         or [[com.fasterxml.jackson.databind.JsonMappingException]]
     */
    def fromJson[T : Manifest]: Try[T] =  JsonUtil.fromJson[T](str)
  }

  /**
   * Implicit conversions from an arbitrary JSON object to a variety of object types.
   *
   * {{{
   *   import com.paypal.cascade.json._
   *   case class JsonPatch(op: String, path: String, value: Option[Any])
   *   case class AnObject(...)
   *   val jsonStr = JsonPatch("add", "/-", Some(AnObject(...)))).toJson.get
   *   val a = jsonStr.fromJson[JsonPatch].get
   *   val inner = a.value.convertValue[Option[AnObject]].get
   * }}}
   *
   * @param convertMe this object
   */
  implicit class Convertible(convertMe: Any) {

    /**
     * Convert an arbitrary JSON object to a `T`, where `T` is some context bound type. Useful for
     * secondary JSON conversion, e.g. for polymorphic data members. A direct use-case
     * is in JSON-Patch, where the expected value may be any of a raw value (String, Int, etc.),
     * a full JSON object, or nothing.
     *
     * @note In general, [[Unmarshallable.fromJson]] should be preferred for String-to-Object conversion.
     *       This method is intended for secondary conversion after [[Unmarshallable.fromJson]] has been
     *       applied.
     *
     * @note This proxy method exists as an alternative to exposing the entire private `mapper` through a
     *       `getInstance` or `copy` method, so that the `mapper` remains a strict singleton and its
     *       configuration remains obscured. Otherwise, this is a direct proxy of the `mapper.convertValue`
     *       method from `ScalaObjectMapper` in Jackson, with added exception catching.
     *
     * @note Discovered via testing, this method does not play well with Optional data stored in `Any` fields.
     *       If you know that you need to serialize Optional values, please state them as Optional in
     *       their type declaration, e.g. in the example here with `Foo` and `Bar`. If a `None` is serialized
     *       in an object with an `Any` field, and you try to convert this as an `Option`, it will instead
     *       come out as a `null` value and you will have to post-process it as such. Instead, if the field
     *       is declared as a `Option[Any]` and a `None` is serialized, it will correctly be converted here
     *       as a `None`.
     *
     * @tparam T a context bound type
     * @return a [[scala.util.Try]] that is either the object of type `T`, or a
     *         [[java.lang.IllegalArgumentException]] in the case of a cast to an incompatible type.
     */
    def convertValue[T : Manifest]: Try[T] = JsonUtil.convertValue[T](convertMe)
  }

  /**
   * Implicit conversions from a given object of type `T` to a JSON String
   *
   * {{{
   *   import com.paypal.cascade.json._
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
     * Convert an object to a JSON string representation.
     *
     * @return a [[scala.util.Try]] that is either the JSON string representation,
     *         or a [[com.fasterxml.jackson.core.JsonProcessingException]]
     */
    def toJson: Try[String] = JsonUtil.toJson(marshallMe)
  }

}
