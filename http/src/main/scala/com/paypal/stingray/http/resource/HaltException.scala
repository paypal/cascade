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
package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.HttpResponse

/**
 * Specialized Exception type that encodes an HTTP error response, including an error code
 */
case class HaltException(response: HttpResponse) extends Exception(s"Halting with response $response")

/**
 * Convenience methods
 */
object HaltException {

  /**
   * Convenience constructor for HaltException that builds an HttpResponse based on given parameters
   * @param status the [[spray.http.StatusCode]] to use
   * @param entity an [[spray.http.HttpEntity]] to include, if any
   * @param headers a list of [[spray.http.HttpHeader]] objects to include, if any
   * @return a fully specified HaltException
   */
  def apply(status: StatusCode,
            entity: HttpEntity = Empty,
            headers: List[HttpHeader] = Nil): HaltException = HaltException(HttpResponse(status, entity, headers))
}
