package com.paypal.stingray.common.values

import java.net.URL
import java.util.{Properties, Date}
import java.text.SimpleDateFormat
import com.paypal.stingray.common.logging.LoggingSugar
import scala.util.Try

/**
 * Static Values class specifically for build.properties dependency list accessor
 * and date value reader.
 *
 */
class BuildStaticValues extends LoggingSugar {

  private val mbUrl = BuildStaticValues.getBuildUrl

  val logger = getLogger[BuildStaticValues]

  /**
   * Retrieves an optional value from a [[java.util.Properties]] object
   * @param key the key to retrieve
   * @return an optional String value for the given `key`
   */
  def get(key: String): Option[String] = props.flatMap(p => Option(p.getProperty(key)))

  /**
   * Retrieves an optional value and attempts to parse it as a [[java.text.SimpleDateFormat]]
   * in the format `yyMMddHHmmssZ`
   * @param s the key to retrieve
   * @return optionally, a decoded Date object, or None if the value does not exist or cannot be parsed
   */
  def getDate(s: String): Option[Date] = {
    get(s).flatMap(value => Try { new SimpleDateFormat("yyMMddHHmmssZ").parse(value) }.toOption)
  }

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

  private val Artifact = """groupId=(\S+), artifactId=(\S+), version=(\S+), type=([^\s\}]+)""".r

  /**
   * Retrieves an optional comma-separated list of dependencies for this artifact.
   * @param s the key to retrieve
   * @return optionally, a list of dependencies, or None if the value does not exist or cannot be parsed as a list
   */
  def getDependencyList(s: String): Option[List[String]] = {
    get(s).map(_.toLowerCase).map({ prop =>
      val artifacts = Artifact.findAllIn(prop).collect {
        case Artifact(gid, aid, vsn, t) if t == "jar" => s"$gid:$aid:$vsn"
      }.toList
      if (artifacts.size > 0) {
        // Look for the Maven format first
        artifacts
      } else {
        // Otherwise, assume the sbt format
        prop.split(",").toList
      }
    })
  }

}

object BuildStaticValues {

  /**
   * A new default properties file location, at `build.properties`
   * @return optionally, an SV URL pointing to `build.properties`, or None if no resource exists with that name
   */
  private def getBuildUrl: Option[URL] = {
    Try { getClass.getResource("build.properties") }.toOption
  }

}
