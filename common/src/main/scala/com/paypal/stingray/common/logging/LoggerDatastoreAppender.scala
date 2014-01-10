package com.paypal.stingray.common.logging

import ch.qos.logback.core.AppenderBase
import ch.qos.logback.classic.spi.LoggingEvent
import com.paypal.stingray.common.constants.CommonConstants
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.constants.ValueConstants._
import com.paypal.stingray.common.values.StaticValues

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 12/10/12
 * Time: 11:36 AM
 */
class LoggerDatastoreAppender extends AppenderBase[LoggingEvent] {

  // We don't generally want to be doing this since it parses the
  // property file again. This is a weird case since we're loaded
  // by logback without a good way to pass on this info
  lazy val svs = StaticValues.defaultValues

  lazy val logger = new LoggerDatastoreService(svs)

  override def append(eventObject: LoggingEvent) {
    logger.logEvent(eventObject)
  }

}
