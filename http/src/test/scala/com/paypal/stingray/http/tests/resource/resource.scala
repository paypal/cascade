package com.paypal.stingray.http.tests

import spray.http.{HttpResponse, HttpRequest}
import com.paypal.stingray.http.resource.{HttpResourceActor, ResourceDriver, AbstractResourceActor}
import scala.concurrent.Future
import scala.util.Try
import akka.actor.{Props, ActorRef, ActorSystem}
import com.paypal.stingray.http.resource.HttpResourceActor.ResourceContext

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
