package com.paypal.stingray.http.tests

import spray.http.{Uri, HttpRequest, HttpResponse}
import scala.concurrent.Future
import com.paypal.stingray.http.tests.resource.ResponseHandlerActor
import com.paypal.stingray.http.resource.{AbstractResource, ResourceDriver}
import scala.util.Try
import akka.actor.ActorSystem
import spray.routing.RequestContext

package object matchers {

  def executeResourceDriver[AuthInfo, ParsedRequest](req: HttpRequest,
                                                     resource: AbstractResource[AuthInfo],
                                                     processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                     requestParser: HttpRequest => Try[ParsedRequest])
                                                    (implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    val (respHandler, respPromise) = ResponseHandlerActor.apply
    val requestContext = RequestContext(req, respHandler, Uri.Path.SingleSlash)
    val fn = ResourceDriver.serve(resource, processFunction, requestParser, Some(respHandler))
    fn(requestContext)
    respPromise.future
  }

}
