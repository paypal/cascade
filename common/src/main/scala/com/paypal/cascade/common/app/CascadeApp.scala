/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.app

import org.slf4j.bridge.SLF4JBridgeHandler
import org.slf4j.LoggerFactory
import com.paypal.cascade.common.logging._

/**
 * Starting point for runnable applications and services. Sets up logging and MDC values.
 * Otherwise functions like [[scala.App]].
 */
trait CascadeApp extends App {

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
