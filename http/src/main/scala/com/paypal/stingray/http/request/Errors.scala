package com.paypal.stingray.http.request

import scalaz.NonEmptyList
import com.paypal.stingray.common.primitives._
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

case class ModulesHeaderNotFoundError() extends InternalRequestToHttpRequestError with HttpRequestToInternalRequestError {
  override lazy val reason = "Modules header not found"
}

case class MalformedModulesHeaderError(hdrString: String) extends HttpRequestToInternalRequestError {
  override lazy val reason = "could not parse modules header %s".format(hdrString)
}
case class InvalidModuleIdsError(invalidModuleIDs: NonEmptyList[ModuleId]) extends HttpRequestToInternalRequestError {
  override lazy val reason = "Invalid module IDs: %s".format(invalidModuleIDs.list.mkString(", "))
}

case object AppIDHeaderNotFoundError extends InternalRequestToHttpRequestError with HttpRequestToInternalRequestError {
  override lazy val reason = "AppID header not found"
}

case class AppIDMalformedError(idString: String) extends InternalRequestToHttpRequestError with HttpRequestToInternalRequestError {
  override lazy val reason = "AppID %s is malformed".format(idString)
}

case object APIVersionHeaderNotFoundError extends InternalRequestToHttpRequestError with HttpRequestToInternalRequestError {
  override lazy val reason = "APIVersion header not found"
}

case object AuthrorizationHeaderNotFoundError extends InternalRequestToHttpRequestError with HttpRequestToInternalRequestError {
  override lazy val reason = "Authorization header not found"
}

case class APIVersionHeaderMalformedError(envString: String) extends InternalRequestToHttpRequestError with HttpRequestToInternalRequestError {
  override lazy val reason = "APIVersion %s is malformed" format envString
}

case class MethodNotSupportedError(method: HttpMethod) extends InternalRequestToHttpRequestError {
  override lazy val reason = "Method %s not supported in Newman".format(method.toString)
}
