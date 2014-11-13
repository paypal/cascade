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
package com.paypal.cascade.http.tests.api

import spray.http._
import spray.http.HttpResponse
import akka.testkit.TestActorRef

/**
 * Provides the client to make requests against Spray routes without starting a server and without incurring
 * any network traffic
 * @param server the server to make requests against
 */
class SprayRoutingClient(server: TestActorRef[SprayRoutingServer]) {

  /**
   * Make a request against the spray routes defined in config
   * @param method Http method to use
   * @param url Relative path indicating route to use
   * @param headers Headers in the request
   * @param body Body of the request, None if no body; defaults to None
   * @return A spray HttpResponse with the server's response
   */
  def makeRequest(method: HttpMethod,
                  url: String,
                  headers: List[HttpHeader],
                  body: Option[HttpEntity] = None): HttpResponse = {
    implicit val system = server.underlyingActor.actorSystemWrapper.system
    server.underlyingActor.makeRequest(method, url, headers, body)
  }
}
