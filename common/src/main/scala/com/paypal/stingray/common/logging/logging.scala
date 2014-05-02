package com.paypal.stingray.common

import org.slf4j.LoggerFactory
import ch.qos.logback.classic.LoggerContext
import com.paypal.stingray.common.util.casts._

/**
 * Convenience methods and implicits for working with logging.
 */
package object logging {

  /**
   * Flush all logs. Useful right before explicit JVM termination.
   */
  def flushAllLogs(): Unit = {
    val factory = LoggerFactory.getILoggerFactory.cast[LoggerContext]
    factory.foreach(_.stop())
  }

}
