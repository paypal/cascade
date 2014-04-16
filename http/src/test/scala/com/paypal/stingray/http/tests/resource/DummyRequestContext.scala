package com.paypal.stingray.http.tests.resource

import spray.http.{HttpResponse, HttpRequest}
import spray.routing.RequestContext
import akka.testkit.TestActorRef
import scala.concurrent.Promise
import akka.actor.ActorSystem

object DummyRequestContext {

  /**
   * create a new dummy RequestContext, using a ResponseHandlerActor as the target for `ctx.complete`
   * @param req the request that the new [[RequestContext]] should contain. the [[spray.http.Uri.Path]] is also determined from this param
   * @return the new [[RequestContext]] as well as the actor that is called when [[RequestContext.complete]] is called
   */
  def apply(req: HttpRequest,
            prom: Promise[HttpResponse] = Promise[HttpResponse]())
           (implicit actorSystem: ActorSystem): (RequestContext, TestActorRef[ResponseHandlerActor]) = {
    val responseHandler = TestActorRef(new ResponseHandlerActor(prom))
    RequestContext(req, responseHandler, req.uri.path) -> responseHandler
  }

}
