package com.paypal.stingray.common.json

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
 * Created by awharris on 1/17/14.
 *
 * Patterns adapted from https://coderwall.com/p/o--apg
 */
object JsonUtil {

  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(value: Map[Symbol, Any]): String = {
    toJson(value map { case (k, v) => k.name -> v})
  }

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }

  def toMap[V: Manifest](json:String) = fromJson[Map[String, V]](json)

  def fromJson[T: Manifest](json: String): T = {
    mapper.readValue[T](json)
  }
}
