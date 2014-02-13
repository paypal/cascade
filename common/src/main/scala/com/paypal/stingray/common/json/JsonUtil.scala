package com.paypal.stingray.common.json

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
 *  - Options do not serialize/deserialize cleanly. Jackson is Java underneath, and so has no concept of an Option.
 *    As a result, Options are serialized as their inner value if Some, or null if None. On deserialization, because
 *    `null` is a valid JSON value, any null values are deserialized as just that: null values.
 *
 *    For case classes that use Options, values that are Options are safe to use. Jackson lifts those into their
 *    correct Option form.
 *
 *    For containers that use Options, e.g. `List[Option[T]]`, Jackson will not ser/deser properly. This will
 *    yield a result such as `List(null)` when `List(None)` is intended.
 */
object JsonUtil {

  // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
  private val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(StingrayScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

  class StingrayScalaModule
    extends DefaultScalaModule
    with StingrayOptionModule {
    override def getModuleName() = "StingrayScalaModule"
  }

  object StingrayScalaModule extends StingrayScalaModule

  trait StingrayOptionModule extends StingrayOptionSerializerModule with StingrayOptionDeserializerModule

}
