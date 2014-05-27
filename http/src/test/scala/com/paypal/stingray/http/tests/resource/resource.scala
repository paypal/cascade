package com.paypal.stingray.http.tests

import spray.http.{HttpResponse, HttpRequest}
import com.paypal.stingray.http.resource.{ResourceDriver, AbstractResourceActor}
import scala.concurrent.Future
import scala.util.Try
import akka.actor.ActorSystem

package object resource {
  /**
   * execute the ResourceDriver
   * @param req the request to execute
   * @param resource the resource to handle the request
   * @param processFunction the function to process the parsed request
   * @param requestParser the function to parse the request into a type, or fail
   * @param actorSystem the actor system from which to create the response handler actor
   * @tparam AuthInfo the authorization that the resource uses
   * @tparam ParsedRequest the type of the successfully parsed request
   * @return a future that will be fulfilled when request parsing is complete
   */
  def executeResourceDriver[AuthInfo, ParsedRequest](req: HttpRequest,
                                                     resource: AbstractResourceActor[AuthInfo],
                                                     processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                     requestParser: HttpRequest => Try[ParsedRequest])
                                                    (implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    val respHandler = ResponseHandlerActor.apply
    val (requestContext, _) = DummyRequestContext(req)
    val fn = ResourceDriver.serve(resource, processFunction, requestParser, Some(respHandler))
    fn(requestContext)
    respHandler.underlyingActor.respPromise.future
  }

}
