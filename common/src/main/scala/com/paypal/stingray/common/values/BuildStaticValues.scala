package com.paypal.stingray.common.values

import java.net.URL
import java.util.Date
import java.text.SimpleDateFormat
import com.paypal.stingray.common.constants.ValueConstants._
import scala.util.Try

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/13/13
 * Time: 6:03 PM
 */
class BuildStaticValues(svs: StaticValues) extends StaticValues(BuildStaticValues.getBuildUrl) {
  def getDate(s: String): Option[Date] = {
    get(s).flatMap(value => Try { new SimpleDateFormat("yyMMddHHmmssZ").parse(value) }.toOption)
  }

  private val Artifact = """groupId=(\S+), artifactId=(\S+), version=(\S+), type=([^\s\}]+)""".r

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

  lazy val getVersionDescription: String =
    s"${svs.stingrayEnvType.stringVal}, tag=${getOrDie(BuildTag)}, date=${getDate("build_date").getOrElse(new Date())}"

}

object BuildStaticValues {
  private def getBuildUrl: Option[URL] = {
    Try { getClass.getResource("build.properties") }.toOption
  }

}
