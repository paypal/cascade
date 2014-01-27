package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.HttpResponse

/**
 * Specialized Exception type that encodes an HTTP error response, including an error code
 */
case class HaltException(response: HttpResponse) extends Exception(s"Halting with response $response")

/**
 * Convenience methods
 */
object HaltException {

  /**
   * Convenience constructor for HaltException that builds an HttpResponse based on given parameters
   * @param status the [[spray.http.StatusCode]] to use
   * @param entity an [[spray.http.HttpEntity]] to include, if any
   * @param headers a list of [[spray.http.HttpHeader]] objects to include, if any
   * @return a fully specified HaltException
   */
  def apply(status: StatusCode,
            entity: HttpEntity = Empty,
            headers: List[HttpHeader] = Nil): HaltException = HaltException(HttpResponse(status, entity, headers))
}
