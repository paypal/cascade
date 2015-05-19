/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.json

import scala.util.Try

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

/**
 * Patterns adapted from https://coderwall.com/p/o--apg
 *
 * Known caveats:
 *  - If Jackson is instructed to deserialize a non-conforming numeric from a String, it will fail, e.g attempting
 *    to deser an Int from "hello". Conversely, if Jackson is instructed to deser a String from a numeric, it will
 *    succeed, e.g. "100" from 100. If an object has an obvious String representation, Jackson will attempt to treat
 *    it as such.
 *
 *  - Null values will be treated as valid JSON. This is because `null` is a valid JSON value. Case classes that will
 *    be serialized/deser'd need to include their own validation to guard against unintentional nulls.
 *
 */
object JsonUtil {

  private val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new JodaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
  mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

  /**
   * Convert an object to a JSON string representation.
   * @param value the object to convert
   * @return a [[scala.util.Try]] that is either the JSON string representation,
   *         or a [[com.fasterxml.jackson.core.JsonProcessingException]]
   */
  def toJson(value: Any): Try[String] = Try {
    mapper.writeValueAsString(value)
  }

  /**
   * Convert a JSON string to a `T`, where `T` is some context bound type.
   * @param json the JSON string
   * @tparam T a context bound type
   * @return a [[scala.util.Try]] that is either the object of type `T`, or one of
   *         `java.io.IOException`, [[com.fasterxml.jackson.core.JsonParseException]],
   *         or [[com.fasterxml.jackson.databind.JsonMappingException]]
   */
  def fromJson[T : Manifest](json: String): Try[T] = Try {
    mapper.readValue[T](json)
  }

  /**
   * Convert an arbitrary JSON object to a `T`, where `T` is some context bound type. Useful for
   * secondary JSON conversion, e.g. for polymorphic data members. A direct use-case
   * is in JSON-Patch, where the expected value may be any of a raw value (String, Int, etc.),
   * a full JSON object, or nothing.
   *
   * {{{
   *   import com.paypal.cascade.json._
   *   case class JsonPatch(op: String, path: String, value: Option[Any])
   *   case class AnObject(...)
   *   val jsonStr = JsonUtil.toJson(JsonPatch("add", "/-", Some(AnObject(...)))).get
   *   val a = JsonUtil.fromJson[JsonPatch](jsonStr).get
   *   val inner = JsonUtil.convertValue[Option[AnObject]](a.value).get
   * }}}
   *
   * @note In general, [[fromJson]] should be preferred for String-to-Object conversion. This method is
   *       intended for secondary conversion after [[fromJson]] has been applied.
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
   * @param obj the object to convert
   * @tparam T a context bound type
   * @return a [[scala.util.Try]] that is either the object of type `T`, or a
   *         `java.lang.IllegalArgumentException` in the case of a cast to an incompatible type.
   */
  def convertValue[T : Manifest](obj: Any): Try[T] = Try {
    mapper.convertValue[T](obj)
  }

}
