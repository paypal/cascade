package com.paypal.stingray.common.random

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
