package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.HttpResponse

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 5/13/13
 * Time: 3:21 PM
 */
case class HaltException(response: HttpResponse) extends Exception(s"Halting with response $response")

object HaltException {
  def apply(status: StatusCode,
            entity: HttpEntity = Empty,
            headers: List[HttpHeader] = Nil): HaltException = HaltException(HttpResponse(status, entity, headers))
}
