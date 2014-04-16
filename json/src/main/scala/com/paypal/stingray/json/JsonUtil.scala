package com.paypal.stingray.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala._
import scala.util.Try

/**
 * Created by awharris on 1/17/14.
 *
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
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

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
   *         [[java.io.IOException]], [[com.fasterxml.jackson.core.JsonParseException]],
   *         or [[com.fasterxml.jackson.databind.JsonMappingException]]
   */
  def fromJson[T : Manifest](json: String): Try[T] = Try {
    mapper.readValue[T](json)
  }

}
