package com.paypal.stingray.http.tests.api

import scalaz._
import Scalaz._
import spray.http._
import akka.actor.Actor
import spray.http.HttpResponse
import akka.testkit.TestActorRef
import java.util.concurrent.{TimeUnit, CountDownLatch}
import spray.routing.RequestContext
import com.paypal.stingray.http.resource.ResourceService
import com.paypal.stingray.http.actor.RootActorSystemComponent

/**
 * Provides the sprayRoutingClient is for use in integration tests to test the full service stack, including spray routes
 */
trait SprayRoutingClientComponent {
  //Dependencies
  this: ResourceService with RootActorSystemComponent =>

  /**
   * the sprayRoutingClient is for use in integration tests to test the full service stack, including spray routes
   */
  //Service Provided
  lazy val sprayRoutingClient: SprayRoutingClient = new BasicSprayRoutingClient

  /**
   * A SprayRoutingClient provides a method for interacting with a spray service as if via HTTP, using the declared routes
   */
  //Interface provided
  trait SprayRoutingClient {
    /**
     * Make a request against the spray routes handled by the mixed in ResourceService
     * @param method Http method to use
     * @param url Relative path indicating route to use
     * @param headers Headers in the request
     * @param body Body of the request, None if no body; defaults to None
     * @return A spray HttpResponse with the server's response
     * @throws IllegalStateException if the request times out
     */
    def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity] = None): HttpResponse
  }

  //Implementation
  private class BasicSprayRoutingClient extends SprayRoutingClient {
    def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity] = None): HttpResponse = {
      TestActorRef(new RequestRunner).underlyingActor.makeRequest(method, url, headers, body)
    }
  }

  //Why is this not exposed with an actor ref? Because this is not properly an actor-backed service
  //This class just needs to be an actor so that we can trick spray into sending us the response
  //It also needs its own state so needs to be spun up for each request
  //This actor is not started conventionally, instead makeRequest() starts it up as a TestActorRef within akka's test framework
  //Taken from Doug's old SprayRoutingHttpClient
  private class RequestRunner extends Actor {
    val latch: CountDownLatch = new CountDownLatch(1)
    var response: Option[HttpResponse] = none

    def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity]): HttpResponse = {
      val req = HttpRequest(method = method, uri = url, headers = headers, entity = body getOrElse HttpEntity.Empty)
      fullRoute(RequestContext(req, self, req.uri.path))
      latch.await(10, TimeUnit.SECONDS)
      response getOrElse (throw new IllegalStateException("Request timed out"))
    }

    def receive = {
      case resp: HttpResponse => {
        response = resp.some
        latch.countDown()
      }
    }
  }

}
