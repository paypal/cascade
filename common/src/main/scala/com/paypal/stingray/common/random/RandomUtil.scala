package com.paypal.stingray.common.random

import com.paypal.stingray.common.json._
import org.slf4j.LoggerFactory
import scala.util.Random

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 3/12/12
 * Time: 6:38 PM
 */

object RandomUtil {

  private val logger = LoggerFactory.getLogger(getClass)

  private val r = new Random(System.currentTimeMillis)

  def pickRandomValue[T](list: List[T]): Option[T] = {
    for {
      _ <- Option(list)
      _ <- list.headOption
    } yield {
      list(r.nextInt(list.length))
    }
  }

  /**
   * Get random jackson deserialized T from map
   */
  def pickRandomValue[T <: AnyRef](map: Map[String, String], klass: Class[T]): Option[T] = {
    pickRandomValue(map, s =>
      try {
        Option(JSONSerialization.deserialize(s, klass))
      } catch {
        case e: JSONException => {
          logger.error("Can't deserialize %s into %s".format(s,  klass.getName, e))
          None
        }
      }
    )
  }

  def pickRandomValue[T <: AnyRef](map: Map[String, String], convert: String => Option[T]): Option[T] = {
    (for {
      _ <- Option(map)
      _ <- map.headOption
      value <- (Random.shuffle(map.values) filter { convert(_).isDefined }).headOption
    } yield {
      value
    }) flatMap { convert(_) }
  }

}
