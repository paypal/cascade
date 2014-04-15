package com.paypal.stingray.http.tests

import spray.http.{HttpRequest, HttpResponse}
import scala.concurrent.Future
import com.paypal.stingray.http.tests.resource.ResponseHandlerActor
import com.paypal.stingray.http.resource.{AbstractResource, ResourceDriver}
import scala.util.Try
import akka.actor.ActorSystem

package object matchers {

  def executeResourceDriver[AuthInfo, ParsedRequest](resource: AbstractResource[AuthInfo],
                                                     processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                     requestParser: HttpRequest => Try[ParsedRequest])
                                                    (implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    val (respHandler, respPromise) = ResponseHandlerActor.apply
    ResourceDriver.serve(resource, processFunction, requestParser, Some(respHandler))
    respPromise.future
  }

}
