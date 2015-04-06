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

import java.util.concurrent.{TimeUnit, CountDownLatch}

import akka.actor.{ActorSelection, Actor}
import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.common.option._
import com.paypal.cascade.http.resource.ResourceService
import com.paypal.cascade.http.server.SprayConfiguration
import spray.http._
import spray.routing.RequestContext

/**
 * SprayRoutingServer is a fake spray server. Clients can simulate requests on the server with `makeRequest`
 * TODO: use ResponseHandlerActor and DummyRequestContext here, so we can eliminate the latch
 * @param sprayConfig the configuration that the fake server should run with
 * @param actorSystemWrapper the actor system information that the server should run with
 */
class SprayRoutingServer(override val sprayConfig: SprayConfiguration,
                         override val actorSystemWrapper: ActorSystemWrapper,
                         serverActorOverride: ActorSelection) extends Actor with ResourceService {
  //waits for the response from spray, see a few lines below
  private val latch: CountDownLatch = new CountDownLatch(1)
  private var response: Option[HttpResponse] = none
  override val actorRefFactory = context

  override protected lazy val serverActor = serverActorOverride

  /**
   * makeRequest assembles an HttpRequest from the given parameters, makes the request against the fake server,
   * and then returns the response
   * @param method the method to request
   * @param url the url of the request
   * @param headers the headers of the request
   * @param body the (optional) body of the request
   * @return the result of the request
   */
  def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity]): HttpResponse = {
    val req = HttpRequest(method = method, uri = url, headers = headers, entity = body getOrElse HttpEntity.Empty)
    fullRoute(RequestContext(req, self, req.uri.path))
    latch.await(10, TimeUnit.SECONDS)
    response.orThrow(new IllegalStateException("Request timed out"))
  }

  override def receive: Actor.Receive = {
    case resp: HttpResponse =>
      response = resp.some
      latch.countDown()
  }
}
