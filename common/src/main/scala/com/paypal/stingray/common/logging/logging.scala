package com.paypal.stingray.common

import org.slf4j.{LoggerFactory, MDC}
import ch.qos.logback.classic.LoggerContext

/**
 * Convenience methods and implicits for working with logging.
 */
package object logging {

  /**
   * Flushes out the buffer associated with the slf4j logger. Useful right before explicit process termination.
   */
  def flushLogger(): Unit = {
    val factory = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    factory.stop()
  }
}
