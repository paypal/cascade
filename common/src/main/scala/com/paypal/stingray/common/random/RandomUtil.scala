package com.paypal.stingray.common.random

import org.slf4j.LoggerFactory
import scala.util.Random

/**
 * Convenience methods for accessing random values inside of certain datatypes
 */

object RandomUtil {

  private val r = new Random(System.currentTimeMillis)

  /**
   * Given a list, choose a random value from that list
   * @param list the list
   * @tparam T the type of values in the list
   * @return a random value of type `T`
   */
  def pickRandomValue[T](list: List[T]): Option[T] = {
    for {
      _ <- Option(list)
      _ <- list.headOption
    } yield {
      list(r.nextInt(list.length))
    }
  }

  /**
   * Given a map of String key-value pairs, for all values which are defined for the `convert` function,
   * choose a random value from the map and apply the `convert` function to it, returning the result.
   * @param map the map of String key-value pairs
   * @param convert the function by which to convert values
   * @tparam T the resultant type after conversion
   * @return a random converted value of type `T`
   */
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
