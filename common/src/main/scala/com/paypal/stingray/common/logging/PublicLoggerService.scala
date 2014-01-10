package com.paypal.stingray.common.logging

import org.slf4j.{Logger, MDC}
import java.lang.StackTraceElement
import com.paypal.stingray.common.primitives._

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 9/27/11
 * Time: 7:30 PM
 */

class PublicLoggerService(logger: Logger, clientId: ClientId, appId: AppId) {

  def trace(msg: String) {
    toTheUser(clientId, appId) {
      logger.trace(msg)
    }
  }

  def trace(msg: String, t: Throwable) {
    toTheUser(clientId, appId) {
      logger.trace(msg, filter(t))
    }
  }

  def debug(msg: String) {
    toTheUser(clientId, appId) {
      logger.debug(msg)
    }
  }

  def debug(msg: String, t: Throwable) {
    toTheUser(clientId, appId) {
      logger.debug(msg, filter(t))
    }
  }

  def info(msg: String) {
    toTheUser(clientId, appId) {
      logger.info(msg)
    }
  }

  def info(msg: String, t: Throwable) {
    toTheUser(clientId, appId) {
      logger.info(msg, filter(t))
    }
  }

  def warn(msg: String) {
    toTheUser(clientId, appId) {
      logger.warn(msg)
    }
  }

  def warn(msg: String, t: Throwable) {
    toTheUser(clientId, appId) {
      logger.warn(msg, filter(t))
    }
  }

  def error(msg: String) {
    toTheUser(clientId, appId) {
      logger.error(msg)
    }
  }

  def error(msg: String, t: Throwable) {
    toTheUser(clientId, appId) {
      logger.error(msg, filter(t))
    }
  }

  private def filter(t: Throwable): Throwable = {
    val internal = new StackTraceElement("com.stackmob.Internal", "internal", "Internal", 0)
    val i = t.getStackTrace.indexWhere(_.getClassName.startsWith("com.stackmob"))
    val stack = t.getStackTrace.slice(0, i) ++ List(internal)
    t.setStackTrace(stack)
    t
  }

  private def toTheUser[T](clientId: ClientId, appId: AppId)(op: => T): T = {
    val logId = PublicLoggerService.getLogId(clientId, appId)
    try {
      MDC.put(PublicLoggerService.MDC_LOG_ID, logId)
      op
    } finally {
      MDC.remove(PublicLoggerService.MDC_LOG_ID)
    }
  }
}

object PublicLoggerService {

  val MDC_LOG_ID = "log_id"

  def getLogId(clientId: ClientId, appId: AppId): String = {
    "%s-%s".format(clientId.toString, appId.toString)
  }
}

