package com.paypal.stingray.common.properties

import java.net.URL
import java.util.Properties
import com.paypal.stingray.common.logging.LoggingSugar
import scala.util.Try

/**
 * Class specifically for accessing values from build.properties.
 *
 */
class BuildProperties extends LoggingSugar {

  private val buildUrl = BuildProperties.getBuildUrl

  val logger = getLogger[BuildProperties]

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

object BuildProperties {

  /**
   * A new default properties file location, at `build.properties`
   * @return optionally, an SV URL pointing to `build.properties`, or None if no resource exists with that name
   */
  private def getBuildUrl: Option[URL] = {
    Try { getClass.getResource("build.properties") }.toOption
  }

}
