package com.paypal.stingray.common.app

import org.slf4j.bridge.SLF4JBridgeHandler
import org.slf4j.MDC

/**
 * Starting point for runnable applications and services. Sets up logging and MDC values.
 * Otherwise functions like [[scala.App]].
 */

trait StingrayApp extends App {

  /** The name of this service */
  val serviceName: String

  MDC.put("service", serviceName)

  // Install the Java Util Logging to SFL4J bridge and delegate all management to SLF4J.
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

}
