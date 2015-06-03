package com.paypal.cascade.akka.actor

import akka.event.LoggingAdapter
import org.slf4j.helpers.{MarkerIgnoringBase, MessageFormatter}

/**
 * Provides an implicit class which lets you treat an Akka [[akka.event.LoggingAdapter]] as a [[org.slf4j.Logger]].<br><br>
 *
 * Due to the ambiguity in the method overloading in the original interface, the implicit class cannot have any methods
 * that take a variable number of `Any`s. If you need to call such a method with `AnyVal`s, you will have to explicitly
 * convert them to `AnyRef`s (i.e. put them in their equivalent Java classes).
 * {{{
 *   info("{0}, {1}, {2}", Array(10, 20, 30).asInstanceOf[Array[AnyRef]]: _*)
 * }}}
 * This is a non-issue if you just use Scala's string interpolation instead of formatted strings.
 */
trait AkkaLoggingAsSlf4j {

  implicit class LoggingAdapterToLogger(akkaLogger: LoggingAdapter) extends MarkerIgnoringBase {  // scalastyle:ignore number.of.methods

    override def warn(msg: String): Unit = akkaLogger.warning(msg)

    override def warn(format: String, arg: Any): Unit = akkaLogger.warning(format, arg)

    override def warn(format: String, arguments: AnyRef*): Unit = akkaLogger.warning(formatArray(format, arguments))

    override def warn(format: String, arg1: Any, arg2: Any): Unit = akkaLogger.warning(format, arg1, arg2)

    override def warn(msg: String, t: Throwable): Unit = akkaLogger.warning(s"$msg\n$t")

    override def isErrorEnabled: Boolean = akkaLogger.isErrorEnabled

    override def getName: String = AkkaLoggingAsSlf4j.this.getClass.getName

    override def isInfoEnabled: Boolean = akkaLogger.isInfoEnabled

    override def isDebugEnabled: Boolean = akkaLogger.isDebugEnabled

    override def isTraceEnabled: Boolean = isDebugEnabled

    override def error(msg: String): Unit = akkaLogger.error(msg)

    override def error(format: String, arg: Any): Unit = akkaLogger.error(format, arg)

    override def error(format: String, arg1: Any, arg2: Any): Unit = akkaLogger.error(format, arg1, arg2)

    override def error(format: String, arguments: AnyRef*): Unit = akkaLogger.error(formatArray(format, arguments))

    override def error(msg: String, t: Throwable): Unit = akkaLogger.error(t, msg)

    override def debug(msg: String): Unit = akkaLogger.debug(msg)

    override def debug(format: String, arg: Any): Unit = akkaLogger.debug(format, arg)

    override def debug(format: String, arg1: Any, arg2: Any): Unit = akkaLogger.debug(format, arg1, arg2)

    override def debug(format: String, arguments: AnyRef*): Unit = akkaLogger.debug(formatArray(format, arguments))

    override def debug(msg: String, t: Throwable): Unit = akkaLogger.debug(s"$msg\n$t")

    override def isWarnEnabled: Boolean = akkaLogger.isWarningEnabled

    override def trace(msg: String): Unit = debug(msg)

    override def trace(format: String, arg: Any): Unit = debug(format, arg)

    override def trace(format: String, arg1: Any, arg2: Any): Unit = debug(format, arg1, arg2)

    override def trace(format: String, arguments: AnyRef*): Unit = debug(format, arguments)

    override def trace(msg: String, t: Throwable): Unit = debug(msg, t)

    override def info(msg: String): Unit = akkaLogger.info(msg)

    override def info(format: String, arg: Any): Unit = akkaLogger.info(format, arg)

    override def info(format: String, arg1: Any, arg2: Any): Unit = akkaLogger.info(format, arg1, arg2)

    override def info(format: String, arguments: AnyRef*): Unit = akkaLogger.info(formatArray(format, arguments))

    override def info(msg: String, t: Throwable): Unit = akkaLogger.info(s"$msg\n$t")
  }

  private[this] def formatArray(format: String, arguments: AnyRef*): String = {
    MessageFormatter.arrayFormat(format, arguments.toArray).getMessage
  }

}
