package com.paypal.stingray.common

import com.typesafe.config._
import scala.collection.JavaConverters._
import com.paypal.stingray.common.util.casts._
import com.paypal.stingray.common.logging._
import scala.concurrent.duration._
import scala.Some

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
 *
 * From inside an actor, you can retrieve the config with
 *
 * {{{
 *   val config = context.system.settings.config
 * }}}
 */
package object config {

  /**
   * Implicit wrapper on Config which provides optional methods on getters.
   *
   * @param underlying Config instance
   */
  implicit class RichConfig(val underlying: Config) extends LoggingSugar {

    private val logger = getLogger[RichConfig]

    /**
     * Private helper which wraps Config getter logic
     *
     * @param f function which performs Config getter
     * @tparam T return type
     * @return Some(typed value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     * @throws ConfigException.BadValue if the config value cannot be parsed correctly
     */
    @throws[ConfigException.WrongType]
    @throws[ConfigException.BadValue]
    private def getOptionalHelper[T](f: => T): Option[T] = {
      try {
        Some(f)
      } catch {
        case _: ConfigException.Missing => None
        case wt: ConfigException.WrongType => {
          logger.error("Config value does not match the request type", wt)
          throw wt
        }
        case bv: ConfigException.BadValue => {
          logger.error("Config value cannot be parsed correctly", bv)
          throw bv
        }
      }
    }

    /**
     * Optional wrapper for String getter.
     *
     * @param path path expression
     * @return Some(String value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     */
    @throws[ConfigException.WrongType]
    def getOptionalString(path: String): Option[String] = getOptionalHelper(underlying.getString(path))

    /**
     * Optional wrapper for Boolean getter.
     *
     * @param path path expression
     * @return Some(Boolean value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     */
    @throws[ConfigException.WrongType]
    def getOptionalBoolean(path: String): Option[Boolean] = getOptionalHelper(underlying.getBoolean(path))

    /**
     * Optional wrapper for Int getter.
     *
     * @param path path expression
     * @return Some(Int value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     */
    @throws[ConfigException.WrongType]
    def getOptionalInt(path: String): Option[Int] = getOptionalHelper(underlying.getInt(path))

    /**
     * Optional wrapper for Long getter.
     *
     * @param path path expression
     * @return Some(Long value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     */
    @throws[ConfigException.WrongType]
    def getOptionalLong(path: String): Option[Long] = getOptionalHelper(underlying.getLong(path))

    /**
     * Optional wrapper for List getter.
     * Assumes and returns only list objects of String type
     *
     * @param path path expression
     * @return Some(List[String] value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     */
    @throws[ConfigException.WrongType]
    def getOptionalList(path: String): Option[List[String]] = {
      val list = getOptionalHelper(underlying.getList(path))
      list.map(_.unwrapped.asScala.toList.cast[String])
    }

    /**
     * Optional wrapper for Duration getter
     *
     * @param path path expression
     * @param tUnit convert the return value to this time unit
     * @return Some(Long value) or None if the path doesn't exist or is set to null
     * @throws ConfigException.WrongType if the config value does not match the request type
     * @throws ConfigException.BadValue if the config value cannot be parsed correctly
     */
    @throws[ConfigException.WrongType]
    @throws[ConfigException.BadValue]
    def getOptionalDuration(path: String, tUnit: TimeUnit): Option[Long] = getOptionalHelper(underlying.getDuration(path, tUnit))

  }

}
