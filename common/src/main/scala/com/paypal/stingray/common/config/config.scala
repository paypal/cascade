package com.paypal.stingray.common

import com.typesafe.config._
import scala.collection.JavaConverters._
import com.paypal.stingray.common.util.casts._

/**
 * Convenience methods and implicit wrappers for working with [[com.typesafe.config]]
 *
 * https://github.com/typesafehub/config
 *
 * ConfigFactory.load() loads the following (first listed are higher priority):
 *
 *    system properties
 *    application.conf (all resources on classpath with this name)
 *    application.json (all resources on classpath with this name)
 *    application.properties (all resources on classpath with this name)
 *    reference.conf (all resources on classpath with this name)
 */
package object config {

  /**
   * Implicit wrapper on Config which provides optional methods on getters.
   *
   * @param underlying Config instance
   */
  implicit class RichConfig(val underlying: Config) extends AnyVal {

    /**
     * Private helper which wraps Config getter logic
     *
     * @param f function which performs Config getter
     * @tparam T return type
     * @return Some(typed value) or None if the path doesn't exist or is set to null
     */
    private def getOptionalHelper[T](f: => T): Option[T] = try {
      Some(f)
    } catch {
      case e: ConfigException.Missing =>
        None
    }

    /**
     * Optional wrapper for String getter.
     *
     * @param path path expression
     * @return Some(String value) or None if the path doesn't exist or is set to null
     */
    def getOptionalString(path: String): Option[String] = getOptionalHelper(underlying.getString(path))

    /**
     * Optional wrapper for Boolean getter.
     *
     * @param path path expression
     * @return Some(Boolean value) or None if the path doesn't exist or is set to null
     */
    def getOptionalBoolean(path: String): Option[Boolean] = getOptionalHelper(underlying.getBoolean(path))

    /**
     * Optional wrapper for Int getter.
     *
     * @param path path expression
     * @return Some(Int value) or None if the path doesn't exist or is set to null
     */
    def getOptionalInt(path: String): Option[Int] = getOptionalHelper(underlying.getInt(path))

    /**
     * Optional wrapper for Long getter.
     *
     * @param path path expression
     * @return Some(Long value) or None if the path doesn't exist or is set to null
     */
    def getOptionalLong(path: String): Option[Long] = getOptionalHelper(underlying.getLong(path))


    /**
     * Optional wrapper for List getter.
     * Assumes and returns only list objects of String type
     *
     * @param path path expression
     * @return Some(List[String] value) or None if the path doesn't exist or is set to null
     */
    def getOptionalList(path: String): Option[List[String]] = {
      val list = getOptionalHelper(underlying.getList(path))
      list.map(_.unwrapped().asScala.toList.cast[String])
    }

  }

}
