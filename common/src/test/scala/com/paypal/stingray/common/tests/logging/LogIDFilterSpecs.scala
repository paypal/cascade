package com.stackmob.tests.common.logging

import org.specs2._
import scala.collection.JavaConverters._
import mock.Mockito
import com.paypal.stingray.common.logging.LogIDFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.FilterReply
import com.paypal.stingray.common.constants.CommonConstants._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 5/30/12
 * Time: 2:30 AM
 */

class LogIDFilterSpecs extends Specification with Mockito { override def is =

  "LogIDFilterSpecs".title                                           ^
    """
    LogIDFilter directs log events going through logback.
    """                                                              ^
    "deny a message with an empty mdc"  ! checkFilterWithEmptyMDC()  ^
    "deny a message with no log id"     ! checkFilterWithoutLogID()  ^
    "deny a message with a null log id" ! checkFilterWithNullLogID() ^
    "return neutral witha good log id"  ! checkFilterWithoutLogID()  ^
                                                                     end

  val filter = new LogIDFilter

  def checkFilterWithEmptyMDC() = {
    val mockedEvent = mock[ILoggingEvent]
    mockedEvent.getMDCPropertyMap returns null
    filter.decide(mockedEvent) mustEqual FilterReply.DENY
  }

  def checkFilterWithoutLogID() = {
    val mockedEvent = mock[ILoggingEvent]
    mockedEvent.getMDCPropertyMap returns Map(MDC_APP_ID -> "1").asJava
    filter.decide(mockedEvent) mustEqual FilterReply.DENY
  }

  def checkFilterWithNullLogID() = {
    val mockedEvent = mock[ILoggingEvent]
    mockedEvent.getMDCPropertyMap returns Map(MDC_LOG_ID -> (null: String)).asJava
    filter.decide(mockedEvent) mustEqual FilterReply.DENY
  }

  def checkFilterWithLogID() = {
    val mockedEvent = mock[ILoggingEvent]
    mockedEvent.getMDCPropertyMap returns Map(MDC_LOG_ID -> "1-1").asJava
    filter.decide(mockedEvent) mustEqual FilterReply.NEUTRAL
  }

}
