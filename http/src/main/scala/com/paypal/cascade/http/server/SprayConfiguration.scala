/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.http.server

import spray.routing.{ExceptionHandler, RejectionHandler, Route}

/**
 * This class provides configuration information for a spray service
 */
class SprayConfiguration(val serviceName: String,
                         val port: Int,
                         val backlog: Int,
                         val route: Route,
                         val customExceptionHandler: Option[ExceptionHandler] = None,
                         val customRejectionHandler: Option[RejectionHandler] = None)

object SprayConfiguration {
  def apply(serviceName: String, port: Int, backlog: Int, customExceptionHandler: Option[ExceptionHandler] = None, customRejectionHandler: Option[RejectionHandler] = None)
           (route: Route): SprayConfiguration = {
    new SprayConfiguration(serviceName, port, backlog, route, customExceptionHandler, customRejectionHandler)
  }
}
