package com.paypal.stingray.http.request

import spray.http.HttpMethod

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/19/13
 * Time: 10:22 AM
 */
abstract class BaseError {
  def reason: String
}
sealed trait HttpRequestToInternalRequestError extends BaseError
sealed trait InternalRequestToHttpRequestError extends BaseError

case class LazyStreamBodyNotSupportedError() extends InternalRequestToHttpRequestError {
  override lazy val reason = "Streaming requests are not supported at this time"
}
