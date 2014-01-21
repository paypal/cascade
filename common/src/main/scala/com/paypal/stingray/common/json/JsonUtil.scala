package com.paypal.stingray.common.json

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.util.Try

/**
 * Created by awharris on 1/17/14.
 *
 * Patterns adapted from https://coderwall.com/p/o--apg
 */
object JsonUtil {

  // TODO: write specs!
  // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  /**
   * Convert a `Map[Symbol, Any]` to a JSON string representation.
   * @param value the map to convert
   * @return a [[scala.util.Try]] that is either the JSON string representation,
   *         or a [[com.fasterxml.jackson.core.JsonProcessingException]]
   */
  def toJson(value: Map[Symbol, Any]): Try[String] = {
    toJson(value map { case (k, v) => k.name -> v})
  }

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
   * Convert a JSON string to a `Map[String, T]`, where `T` is some context bound type.
   * @param json the JSON string
   * @tparam T a context bound type
   * @return a [[scala.util.Try]] that is either the `Map[String, T]`, or one of
   *         [[java.io.IOException]], [[com.fasterxml.jackson.core.JsonParseException]],
   *         or [[com.fasterxml.jackson.databind.JsonMappingException]]
   */
  def fromJsonToMap[T : Manifest](json: String): Try[Map[String, T]] = {
    fromJson[Map[String, T]](json)
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
