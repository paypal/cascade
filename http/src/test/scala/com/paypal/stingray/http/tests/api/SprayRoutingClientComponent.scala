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

trait SprayRoutingClientComponent {
  //Dependencies
  this: ResourceService with RootActorSystemComponent =>

  //Service Provided
  val sprayRoutingClient: SprayRoutingClient = new BasicSprayRoutingClient

  //Interface provided
  trait SprayRoutingClient {
    def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity]): HttpResponse
  }

  //Implementation
  class BasicSprayRoutingClient extends SprayRoutingClient {
    def makeRequest(method: HttpMethod, url: String, headers: List[HttpHeader], body: Option[HttpEntity]): HttpResponse = {
      TestActorRef(new RequestRunner).underlyingActor.makeRequest(method, url, headers, body)
    }
  }

  //Why is this not exposed with an actor ref? Because this is not properly an actor-backed service
  //This class just needs to be an actor so that we can trick spray into sending us the response
  //It also needs its own state so needs to be spun up for each request
  //Taken from Doug's old SprayRoutingHttpClient
  class RequestRunner extends Actor {
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
