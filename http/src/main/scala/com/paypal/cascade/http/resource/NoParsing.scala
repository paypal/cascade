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
package com.paypal.cascade.http.resource

import spray.http.HttpRequest
import scala.concurrent.Future

/**
 * Mix this into an [[com.paypal.cascade.http.resource.AbstractResourceActor]] implementation
 * and use [[spray.http.HttpRequest]] as the `ParsedRequest` type to perform no additional parsing
 * of incoming requests. Useful for status endpoints, etc.
 */
trait NoParsing {

  /**
   * Performs no parsing
   * @param r the request
   * @param pathParts the parts of the request path
   * @return an unparsed request
   */
  def parseRequest(r: HttpRequest, pathParts: Map[String, String]): Future[HttpRequest] = r.continue

}
