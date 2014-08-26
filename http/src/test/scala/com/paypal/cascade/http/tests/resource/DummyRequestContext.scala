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
package com.paypal.cascade.http.tests.resource

import spray.http.{HttpResponse, HttpRequest}
import spray.routing.RequestContext
import akka.testkit.TestActorRef
import scala.concurrent.Promise
import akka.actor.ActorSystem

object DummyRequestContext {

  /**
   * create a new [[spray.routing.RequestContext]] to pass into [[com.paypal.cascade.http.resource.HttpResourceActor]]s for testing purposes.
   * the context will send a message to a [[com.paypal.cascade.http.tests.resource.ResponseHandlerActor]] when `ctx.complete` is called on it.
   * that functionality is useful for testing to ensure that the [[com.paypal.cascade.http.resource.HttpResourceActor]] completed
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
