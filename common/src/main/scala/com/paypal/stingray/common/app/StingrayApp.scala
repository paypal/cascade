package com.paypal.stingray.common.app

import org.slf4j.bridge.SLF4JBridgeHandler
import org.slf4j.{LoggerFactory, MDC}
import ch.qos.logback.classic.LoggerContext

/**
 * Starting point for runnable applications and services. Sets up logging and MDC values.
 * Otherwise functions like [[scala.App]].
 */
trait StingrayApp extends App {

  // properly kill app for unhandled, unsupervised exceptions
  Thread.currentThread.setUncaughtExceptionHandler(
    new Thread.UncaughtExceptionHandler() {
      def uncaughtException(thread: Thread, cause: Throwable): Unit = {
        val errMsg = s"Uncaught error from thread [${thread.getName}]. Shutting down JVM."

        // print to both logs and console
        val logger = LoggerFactory.getLogger(this.getClass)
        logger.error(errMsg, cause)
        System.err.println(errMsg)
        cause.printStackTrace(System.err)

        // flush logger and exit
        val factory = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
        factory.stop()
        System.exit(-1)
      }
    }
  )

  /** The name of this service */
  val serviceName: String

  MDC.put("service", serviceName)

  // Install the Java Util Logging to SFL4J bridge and delegate all management to SLF4J.
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

}
