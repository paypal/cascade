package com.paypal.stingray.common.json

import org.codehaus.jackson._
import map.SerializationConfig.Feature
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException
import org.codehaus.jackson.node.ArrayNode
import org.codehaus.jackson.node.JsonNodeFactory
import org.codehaus.jackson.node.ObjectNode
import org.codehaus.jackson.`type`.TypeReference
import scala.util.control.Exception._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.StringWriter
import java.util.{List => JList}

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/12/12
 * Time: 6:38 PM
 */

object JSONSerialization {

  private val logger = LoggerFactory.getLogger(getClass)

  private val mapper = {
    val mapper = new ObjectMapper
    mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true)
    mapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true)
    mapper
  }

  private val prettyMapper = {
    val mapper = new ObjectMapper
    mapper.configure(Feature.INDENT_OUTPUT, true)
    mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true)
    mapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true)
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper
  }

  @throws(classOf[JSONException])
  def deserialize[T](json: String, typeReference: TypeReference[T]): T = {
    try {
      mapper.readValue(json, typeReference).asInstanceOf[T]
    } catch {
      case e: JsonParseException => throw failed(e, message = "JSONSerialization.deserialize: failed: " + json)
      case e: IOException => throw failed(e)
    }
  }

  @throws(classOf[JSONException])
  def deserialize[T](json: String, clazz: Class[T]): T = {
    deserialize(json, clazz, false)
  }

  @throws(classOf[JSONException])
  def deserialize[T](json: String, clazz: Class[T], suppressErrors: Boolean): T = {
    try {
      mapper.readValue(json, clazz)
    } catch {
      case e: UnrecognizedPropertyException => {
        if (!suppressErrors) {
          logger.warn("Bad data, will try more lenient deser: " + e.getMessage)
        }
        try {
          prettyMapper.readValue(json, clazz)
        } catch {
          case e: IOException => throw failed(e, suppressErrors, "JSONSerialization.deserialize: failed: " + json)
        }
      }
      case e: IOException => throw failed(e, suppressErrors, "JSONSerialization.deserialize: failed: " + json)
    }
  }

  @throws(classOf[JSONException])
  def deserializeToJsonTree(json: String): JsonNode = {
    deserializeToJsonTree(json, false)
  }

  @throws(classOf[JSONException])
  def deserializeToJsonTree(json: String, suppressErrors: Boolean): JsonNode = {
    deserialize(json, classOf[JsonNode], suppressErrors)
  }

  @throws(classOf[JSONException])
  def deserializeToJsonArray(jsonList: JList[String]): ArrayNode = {
    if (Option(jsonList).isEmpty) { throw new JSONException("null") }

    jsonList.asScala.foldLeft(JsonNodeFactory.instance.arrayNode)((r, s) => {
      if (Option(s).isEmpty) { throw new JSONException("null") }
      r.add(JSONSerialization.deserializeToJsonTree(s))
      r
    })
  }

  @throws(classOf[JSONException])
  def deserializeToJsonArray(json: String): ArrayNode = {
    deserializeToJsonTree(json) match {
      case n: ArrayNode => n
      case n => throw new JSONException("Expected ArrayNode but got " + n.getClass.getSimpleName)
    }
  }

  @throws(classOf[JSONException])
  def deserializeToJsonObject(json: String): ObjectNode = {
    deserializeToJsonObject(json, false)
  }

  @throws(classOf[JSONException])
  def deserializeToJsonObject(json: String, suppressErrors: Boolean): ObjectNode = {
    deserializeToJsonTree(json, suppressErrors) match {
      case n: ObjectNode => n
      case n => throw new JSONException("Expected ObjectNode but got " + n.getClass.getSimpleName)
    }
  }

  @throws(classOf[JSONException])
  def serialize(o: AnyRef): String = {
    val w = new StringWriter
    try {
      mapper.writeValue(w, o)
      w.toString
    } catch {
      case e: IOException => throw failed(e)
    }
  }

  @throws(classOf[JSONException])
  def prettyPrint(o: AnyRef): String = {
    val w = new StringWriter
    try {
      prettyMapper.writeValue(w, o)
      w.toString
    } catch {
      case e: IOException => throw failed(e)
    }
  }

  def isValidJson(json: String): Boolean = {
    catching(classOf[JSONException]).withTry { deserializeToJsonTree(json, true) }.isSuccess
  }

  private def failed(inner: Throwable, suppressErrors: Boolean = false, message: String = ""): JSONException = {
    val msg = if (message != "") message else inner.getMessage
    if (!suppressErrors) {
      logger.error(msg, inner)
    }
    new JSONException(msg, inner)
  }

}
