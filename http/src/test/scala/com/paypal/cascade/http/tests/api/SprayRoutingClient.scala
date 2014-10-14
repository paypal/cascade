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

import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.http.resource.ResourceService
import com.paypal.cascade.http.server.SprayConfiguration
import spray.http._
import akka.actor.Actor
import spray.http.HttpResponse
import akka.testkit.TestActorRef
import java.util.concurrent.{TimeUnit, CountDownLatch}
import spray.routing.RequestContext
import com.paypal.cascade.common.option._

/**
 * Provides the client to make requests against Spray routes without starting a server and without incurring
 * any network traffic
 * @param config The configuration of the Spray server, including the routes.
 *               Since this method won't start up a real server, the port and backlog
 *               members of this object are ignored.
 * @param actorSystemWrapper The wrapper for the test-wide ActorSystem. Use one ActorSystemWrapper for your whole test.
 */
class SprayRoutingClient(config: SprayConfiguration, actorSystemWrapper: ActorSystemWrapper) {

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
    implicit val system = actorSystemWrapper.system
    val testActor = TestActorRef(new RequestRunner(config, actorSystemWrapper))
    testActor.underlyingActor.makeRequest(method, url, headers, body)
  }

  //Why is this not exposed with an actor ref? Because this is not properly an actor-backed service
  //This class just needs to be an actor so that we can trick spray into sending us the response
  //It also needs its own state so needs to be spun up for each request
  //This actor is not started conventionally, instead makeRequest() starts it up as a TestActorRef within akka's test framework
  //Taken from Doug's old SprayRoutingHttpClient
  //TODO: use ResponseHandlerActor and DummyRequestContext here, so we can eliminate the latch
  private class RequestRunner(override val config: SprayConfiguration,
                              override val actorSystemWrapper: ActorSystemWrapper) extends Actor with ResourceService {
    //waits for the response from spray, see a few lines below
    private val latch: CountDownLatch = new CountDownLatch(1)
    private var response: Option[HttpResponse] = none
    override val actorRefFactory = context

    def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity]): HttpResponse = {
      val req = HttpRequest(method = method, uri = url, headers = headers, entity = body getOrElse HttpEntity.Empty)
      fullRoute(RequestContext(req, self, req.uri.path))
      latch.await(10, TimeUnit.SECONDS)
      response getOrElse (throw new IllegalStateException("Request timed out"))
    }

    override def receive: Actor.Receive = {
      case resp: HttpResponse =>
        response = resp.some
        latch.countDown()
    }
  }

}
