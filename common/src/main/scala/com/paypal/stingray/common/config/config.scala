package com.paypal.stingray.common

import com.typesafe.config._

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
     * Optional wrapper for string getter.
     *
     * @param path path expression
     * @return Some(string value) or None if the path doesn't exist or is set to null
     */
    def getOptionalString(path: String): Option[String] = try {
      Some(underlying.getString(path))
    } catch {
      case e: ConfigException.Missing =>
        None
    }

    /**
     * Optional wrapper for boolean getter.
     *
     * @param path path expression
     * @return Some(boolean value) or None if the path doesn't exist or is set to null
     */
    def getOptionalBoolean(path: String): Option[Boolean] = try {
      Some(underlying.getBoolean(path))
    } catch {
      case e: ConfigException.Missing =>
        None
    }

    /**
     * Optional wrapper for int getter.
     *
     * @param path path expression
     * @return Some(int value) or None if the path doesn't exist or is set to null
     */
    def getOptionalInt(path: String): Option[Int] = try {
      Some(underlying.getInt(path))
    } catch {
      case e: ConfigException.Missing =>
        None
    }

    /**
     * Optional wrapper for long getter.
     *
     * @param path path expression
     * @return Some(long value) or None if the path doesn't exist or is set to null
     */
    def getOptionalLong(path: String): Option[Long] = try {
      Some(underlying.getLong(path))
    } catch {
      case e: ConfigException.Missing =>
        None
    }

  }

}
