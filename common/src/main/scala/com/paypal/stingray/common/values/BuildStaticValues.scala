package com.paypal.stingray.common.values

import java.net.URL
import java.util.Date
import java.text.SimpleDateFormat
import scala.util.Try

/**
 * Extension of [[com.paypal.stingray.common.values.StaticValues]] that adds a dependency list accessor
 * and a date value reader.
 *
 * @param svs a basic StaticValues that is used for all other SVs
 */
class BuildStaticValues(svs: StaticValues) extends StaticValues(BuildStaticValues.getBuildUrl) {

  /**
   * Retrieves an optional value and attempts to parse it as a [[java.text.SimpleDateFormat]]
   * in the format `yyMMddHHmmssZ`
   * @param s the key to retrieve
   * @return optionally, a decoded Date object, or None if the value does not exist or cannot be parsed
   */
  def getDate(s: String): Option[Date] = {
    get(s).flatMap(value => Try { new SimpleDateFormat("yyMMddHHmmssZ").parse(value) }.toOption)
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
