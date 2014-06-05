package com.paypal.stingray.http.resource

import spray.http.HttpRequest
import scala.concurrent.Future

/**
 * Mix this into an [[com.paypal.stingray.http.resource.AbstractResourceActor]] implementation
 * and use [[spray.http.HttpRequest]] as the `ParsedRequest` type to perform no additional parsing
 * of incoming requests. Useful for status endpoints, etc.
 */
trait NoParsing {

  /**
   * Performs no parsing
   * @param r the request
   * @param pathParts the parts of the request path
   * @return an unparsed request
   */
  def parseRequest(r: HttpRequest, pathParts: Map[String, String]): Future[HttpRequest] = r.continue

}
