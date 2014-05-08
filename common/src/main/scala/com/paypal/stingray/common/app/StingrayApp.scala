package com.paypal.stingray.common.app

import org.slf4j.bridge.SLF4JBridgeHandler
import org.slf4j.LoggerFactory
import com.paypal.stingray.common.logging._

/**
 * Starting point for runnable applications and services. Sets up logging and MDC values.
 * Otherwise functions like [[scala.App]].
 */
trait StingrayApp extends App {

  // Install the Java Util Logging to SFL4J bridge and delegate all management to SLF4J.
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  // properly kill app for unhandled, unsupervised exceptions
  Thread.currentThread.setUncaughtExceptionHandler(
    new Thread.UncaughtExceptionHandler() {
      override def uncaughtException(thread: Thread, cause: Throwable): Unit = {
        val errMsg = s"Uncaught error from thread [${thread.getName}]. Shutting down JVM."

        // print to both logs and console
        val logger = LoggerFactory.getLogger(this.getClass)
        logger.error(errMsg, cause)
        System.err.println(errMsg) // scalastyle:ignore regex
        cause.printStackTrace(System.err)
        System.err.flush()

        // flush logger and exit
        flushAllLogs()
        System.exit(-1)
      }
    }
  )

}
