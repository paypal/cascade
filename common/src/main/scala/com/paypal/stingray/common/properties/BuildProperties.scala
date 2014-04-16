package com.paypal.stingray.common.properties

import java.util.Properties
import com.paypal.stingray.common.logging.LoggingSugar
import scala.util.Try

/**
 * Class specifically for accessing values from build.properties.
 *
 */
class BuildProperties extends LoggingSugar {

  /**
   * A new default properties file location, at `build.properties`, or None if no resource exists with that name
   */
  private val buildUrl = Try(getClass.getResource("/build.properties")).toOption

  // at first use, try to retrieve a Properties object
  private lazy val props: Option[Properties] = {
    lazy val p = new Properties
    for {
      url <- buildUrl
      stream <- Try(url.openStream()).toOption
    } yield {
      p.load(stream)
      p
    }
  }

  /**
   * Retrieves an optional value from a [[java.util.Properties]] object
   * @param key the key to retrieve
   * @return an optional String value for the given `key`
   */
  def get(key: String): Option[String] = props.flatMap(p => Option(p.getProperty(key)))

}
