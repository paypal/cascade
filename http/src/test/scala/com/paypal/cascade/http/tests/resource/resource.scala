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
package com.paypal.cascade.http.tests

import spray.http.{HttpResponse, HttpRequest}
import com.paypal.cascade.http.resource.{HttpResourceActor, ResourceDriver, AbstractResourceActor}
import scala.concurrent.Future
import akka.actor.ActorSystem
import com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext

package object resource {
  /**
   * execute the ResourceDriver
   * @param req the request to execute
   * @param resourceProps the props to create the resource to handle the request
   * @param requestParser the function to parse the request into a type, or fail
   * @param actorSystem the actor system from which to create the response handler actor
   * @return a future that will be fulfilled when request parsing is complete
   */
  def executeResourceDriver(req: HttpRequest,
                            resourceProps: ResourceContext => AbstractResourceActor,
                            requestParser: HttpResourceActor.RequestParser)
                            (implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    val respHandler = ResponseHandlerActor.apply
    val (requestContext, _) = DummyRequestContext(req)
    val fn = ResourceDriver.serve(resourceProps, requestParser, Some(respHandler))
    val respFuture = respHandler.underlyingActor.respPromise.future
    fn(requestContext)
    respFuture
  }

}
