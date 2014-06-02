package com.paypal.stingray.http.tests

import spray.http.{HttpResponse, HttpRequest}
import com.paypal.stingray.http.resource.{ResourceDriver, AbstractResourceActor}
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
   * @tparam AuthInfo the authorization that the resource uses
   * @tparam ParsedRequest the type of the successfully parsed request
   * @return a future that will be fulfilled when request parsing is complete
   */
  def executeResourceDriver[AuthInfo, ParsedRequest](req: HttpRequest,
                                                     resourceProps: ResourceContext => AbstractResourceActor,
                                                     requestParser: HttpRequest => Try[ParsedRequest])
                                                    (implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    val respHandler = ResponseHandlerActor.apply
    val (requestContext, _) = DummyRequestContext(req)
    val fn = ResourceDriver.serve(resourceProps, requestParser, Some(respHandler))
    fn(requestContext)
    respHandler.underlyingActor.respPromise.future
  }

}
