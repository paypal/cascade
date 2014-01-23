package com.paypal.stingray.common.values

import java.net.URL
import java.util.Properties
import java.io.File
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import scala.util.Try

/**
 * An implementation of [[com.paypal.stingray.common.values.Values]] to read synchronously from local properties files.
 *
 * StaticValues should be preferred only for mission-critical values, where a service cannot be started without a real
 * value and no sane default exists. Methods labeled `OrDie` will log to error and throw, if a value is not found.
 *
 * If no URL is given, StaticValues will attempt to find a properties file URL at the following locations, in order:
 *  1) A system property named {{{$serviceName.config}}}
 *  2) A class-loader resource named {{{$serviceName.properties}}}
 *  3) A class resource named {{{$serviceName-default.properties}}}
 *  4) A system property named {{{stingray.cluster.config}}}
 *
 * If a properties file cannot be found, calls made to StaticValues will always return None (or, with `OrDie`, throw).
 */
class StaticValues(mbUrl: Option[URL])
  extends Values
  with LoggingSugar {

  /**
   * Creates a StaticValues with a given properties file URL
   * @param url the properties file URL
   * @return a StaticValues
   */
  def this(url: URL) = this(Option(url))

  /**
   * Creates a StaticValues with a given service name
   * @param serviceName the service name
   * @return a StaticValues
   */
  def this(serviceName: String) = this(StaticValues.getServiceUrl(Some(serviceName)))

  /**
   * Creates a StaticValues with default location values
   * @return a StaticValues
   */
  def this() = this(StaticValues.getServiceUrl(None))

  val logger = getLogger[StaticValues]

  // at first use, try to retrieve a Properties object
  private lazy val props: Option[Properties] = {
    lazy val p = new Properties
    for {
      url <- mbUrl
      stream <- Try(url.openStream()).toOption
    } yield {
      p.load(stream)
      p
    }
  }

  /**
   * Retrieves a value for a given key, or throws
   * @param key the key to retrieve
   * @return the value
   * @throws IllegalStateException if the value is not found
   */
  def getOrDie(key: String): String = {
    get(key).orThrow(lookupFailed(key))
  }

  /**
   * Retrieves a value for a given key and parses it to an Int, or throws
   * @param key the key to retrieve
   * @return the Int value
   * @throws IllegalStateException if the value is not found or cannot be parsed as an Int
   */
  def getIntOrDie(key: String): Int = {
    getInt(key).orThrow(lookupFailed(key))
  }

  /**
   * Retrieves a value for a given key and parses it to a Long, or throws
   * @param key the key to retrieve
   * @return the Long value
   * @throws IllegalStateException if the value is not found or cannot be parsed as a Long
   */
  def getLongOrDie(key: String): Long = {
    getLong(key).orThrow(lookupFailed(key))
  }

  private def lookupFailed(key: String) = {
    val msg = s"Failed to lookup mission critical values from property files $key!!!!!!"
    logger.error(msg)
    new IllegalStateException(msg)
  }

  /**
   * Retrieves an optional value from a [[java.util.Properties]] object
   * @param key the key to retrieve
   * @return an optional String value for the given `key`
   */
  def get(key: String): Option[String] = props.flatMap(p => Option(p.getProperty(key)))
}

object StaticValues {
  /**
   * A StaticValues created from a parameterless instantiation.
   */
  lazy val defaultValues = new StaticValues()

  /**
   * Attempts to locate a property file local to this service, optionally returning a URL to that file
   * @param serviceName optionally, the name of this service
   * @return an optional URL to a property file for this service
   */
  def getServiceUrl(serviceName: Option[String]): Option[URL] = {
    Try(
      serviceName.flatMap(s => Option(System.getProperty(s"$s.config")).map(new File(_).toURI.toURL)) orElse
        serviceName.flatMap(s => Option(getClass.getClassLoader.getResource(s"$s.properties"))) orElse
        serviceName.flatMap(s => Option(getClass.getResource(s"$s-default.properties"))) orElse
        Option(System.getProperty("stingray.cluster.config")).map(new File(_).toURI.toURL)
    ).toOption.flatten
  }

}
