/**
 * Copyright 2013-2015 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.akka

import com.typesafe.config._
import scala.collection.JavaConverters._
import com.paypal.cascade.common.util.casts._
import com.paypal.cascade.common.logging._
import scala.concurrent.duration._

/**
 * Convenience methods and implicit wrappers for working with [[com.typesafe.config.Config]]
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
   * Implicit wrapper on Option to getOrElse throw ConfigError.
   *
   * @param option optionally-wrapped object
   * @tparam T type of object
   */
  implicit class RichConfigOption[T](option: Option[T]) {

    /**
     * Does a getOrElse, throws a ConfigError on else.
     *
     * @param name The name of the error
     * @return Some(T)
     * @throws ConfigError if None
     */
    @throws[ConfigError]
    def orThrowConfigError(name: String): T = option.getOrElse(throw new ConfigError(name))

  }

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
        case wt: ConfigException.WrongType =>
          logger.error("Config value does not match the request type", wt)
          throw wt
        case bv: ConfigException.BadValue =>
          logger.error("Config value cannot be parsed correctly", bv)
          throw bv
        case e: Exception =>
          logger.error(e.getMessage, e)
          throw e
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
