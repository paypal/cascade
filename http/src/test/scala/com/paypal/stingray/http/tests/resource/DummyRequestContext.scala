package com.paypal.stingray.http.tests.resource

import spray.http.{HttpResponse, HttpRequest}
import spray.routing.RequestContext
import akka.testkit.TestActorRef
import scala.concurrent.Promise
import akka.actor.ActorSystem

object DummyRequestContext {

  /**
   * create a new [[spray.routing.RequestContext]] to pass into [[com.paypal.stingray.http.resource.ResourceActor]]s for testing purposes.
   * the context will send a message to a [[ResponseHandlerActor]] when `ctx.complete` is called on it.
   * that functionality is useful for testing to ensure that the [[com.paypal.stingray.http.resource.ResourceActor]] completed
   * @param req the request that the new [[spray.routing.RequestContext]] should contain. the [[spray.http.Uri.Path]] is also determined from this param
   * @return the new [[spray.routing.RequestContext]] as well as the actor that is called when [[spray.routing.RequestContext.complete]] is called
   */
  def apply(req: HttpRequest,
            prom: Promise[HttpResponse] = Promise[HttpResponse]())
           (implicit actorSystem: ActorSystem): (RequestContext, TestActorRef[ResponseHandlerActor]) = {
    val responseHandler = TestActorRef(new ResponseHandlerActor(prom))
    RequestContext(req, responseHandler, req.uri.path) -> responseHandler
  }

}
