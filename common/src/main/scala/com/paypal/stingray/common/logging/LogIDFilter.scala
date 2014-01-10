package com.paypal.stingray.common.logging

import ch.qos.logback.core.filter.Filter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.FilterReply
import com.paypal.stingray.common.constants.CommonConstants
import scalaz._
import Scalaz._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 9/27/11
 * Time: 10:24 AM
 */

class LogIDFilter extends Filter[ILoggingEvent] {

  override def decide(event: ILoggingEvent): FilterReply = {
    (for {
      mdc <- Option(event.getMDCPropertyMap)
      _ <- Option(mdc.get(CommonConstants.MDC_LOG_ID))
    } yield FilterReply.NEUTRAL)| FilterReply.DENY
  }
}
