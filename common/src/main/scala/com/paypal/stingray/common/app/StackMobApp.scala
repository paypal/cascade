package com.paypal.stingray.common.app

import org.slf4j.bridge.SLF4JBridgeHandler
import org.slf4j.MDC

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 3/4/13
 * Time: 12:25 PM
 */

trait StackMobApp extends App {

  def serviceName: String

  MDC.put("service", serviceName)

  // Install the Java util logging to SFL4J bridge and delegate all management to SLF4J.
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

}
