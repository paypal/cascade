package com.paypal.stingray.common.values

import java.net.URL
import java.util.Properties
import java.io.File
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import scala.util.Try

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/13/13
 * Time: 5:50 PM
 */
class StaticValues(mbUrl: Option[URL])
  extends Values
  with LoggingSugar {

  def this(url: URL) = this(Option(url))
  def this(serviceName: String) = this(StaticValues.getServiceUrl(Some(serviceName)))
  def this() = this(StaticValues.getServiceUrl(None))

  val logger = getLogger[StaticValues]

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
   * Only use for mission critical things where we simply can't function without
   * a real vaue and there's no default. Logs and throws a scary error if the
   * value isn't there.
   * @param key the key to get
   * @return
   */
  def getOrDie(key: String): String = {
    get(key).orThrow(lookupFailed(key))
  }

  def getIntOrDie(key: String): Int = {
    getInt(key).orThrow(lookupFailed(key))
  }

  def getLongOrDie(key: String): Long = {
    getLong(key).orThrow(lookupFailed(key))
  }

  private def lookupFailed(key: String) = {
    val msg = s"Failed to lookup mission critical values from property files $key!!!!!!"
    logger.error(msg)
    new IllegalStateException(msg)
  }

  def get(key: String): Option[String] = props.flatMap(p => Option(p.getProperty(key)))
}

object StaticValues {
  lazy val defaultValues = new StaticValues()

  def getServiceUrl(serviceName: Option[String]): Option[URL] = {
    Try(
      serviceName.flatMap(s => Option(System.getProperty(s"$s.config")).map(new File(_).toURI.toURL)) orElse
        serviceName.flatMap(s => Option(getClass.getClassLoader.getResource(s"$s.properties"))) orElse
        serviceName.flatMap(s => Option(getClass.getResource(s"$s-default.properties"))) orElse
        Option(System.getProperty("stingray.cluster.config")).map(new File(_).toURI.toURL)
    ).toOption.flatten
  }

}
