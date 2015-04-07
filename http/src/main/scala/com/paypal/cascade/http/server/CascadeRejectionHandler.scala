package com.paypal.cascade.http.server

import spray.http.StatusCodes._
import spray.routing.{MissingQueryParamRejection, RejectionHandler}
import spray.routing.directives.RouteDirectives._

object CascadeRejectionHandler {
  val handler = RejectionHandler {
    case MissingQueryParamRejection(paramName) :: _ =>
      // override spray default, which returns a 404
      complete(BadRequest, s"Request is missing required query parameter '$paramName'")
  }
}
