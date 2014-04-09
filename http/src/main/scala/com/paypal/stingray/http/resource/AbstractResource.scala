package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.HttpMethods._
import spray.http.HttpResponse
import StatusCodes._
import com.paypal.stingray.common.json._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.concurrent._
import scala.util.{Success, Failure, Try}
import org.slf4j.LoggerFactory

/**
 * Base class for HTTP resources built with Spray.
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 *
 * @tparam AuthInfo a structure for information gained during authorization.
 *                  Use the type [[com.paypal.stingray.http.resource.NoAuth]]
 *                  and trait [[com.paypal.stingray.http.resource.AlwaysAuthorized]] to skip authorization
 */
abstract class AbstractResource[AuthInfo] {

  protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Whether this service is available, by default true
   * @return true if available, false otherwise
   */
  def available: Boolean = true

  /**
   * HTTP methods to accept in this server, by default: GET, PUT, POST, DELETE, HEAD
   * @return a list of the allowed HTTP methods
   */
  def supportedHttpMethods: List[HttpMethod] = List(GET, PUT, POST, DELETE, HEAD)

  /**
   * Attempt to parse the incoming request. If this is CPU intensive, use a background thread,
   * actor running on a dedicated thread, etc
   * @param r the full request
   * @param data the piece of data that should be converted to the suggested type
   * @return the parsed request, or a Failure response
   */
  def parseType[T : Manifest](r: HttpRequest, data: String): Try[T] = {
    JsonUtil.fromJson[T](data)
  }

  def parseType[T : Manifest](r: HttpRequest, data: Array[Byte]): Try[T] = {
    parseType(r, new String(data, charsetUtf8))
  }

  /**
   * The message to be sent back with the `WWW-Authenticate` header when the request is
   * unauthorized. This particular form works around a known Android quirk.
   *
   * See discussion at http://stackoverflow.com/questions/6114455/
   * @return the message
   */
  def unauthorizedChallenge(req: HttpRequest): List[HttpChallenge] =
    List(HttpChallenge("OAuth", req.uri.authority.host.toString))

  /**
   * Determines the AuthInfo for a given request, if authorized
   * @param r the parsed request
   * @return optionally, the AuthInfo for this request, or a Failure(halt)
   */
  def isAuthorized(r: HttpRequest): Future[Option[AuthInfo]]

  /**
   * Whether an incoming request is forbidden to execute, by default false
   * @param r the parsed request
   * @return Failure(halt) if forbidden, false if not
   */
  def isForbidden(r: HttpRequest): Try[Boolean] = Success(false)
  def isForbidden(r: HttpRequest, auth: AuthInfo): Try[Boolean] = isForbidden(r)

  /**
   * A list of content types that that this server can accept, by default `application/json`.
   * These will be matched against the `Content-Type` header of incoming requests.
   * @return a list of content types
   */
  lazy val acceptableContentTypes: List[ContentType] = List(ContentTypes.`application/json`)

  /**
   * The content type that this server provides, by default `application/json`
   * @return a list of content types
   */
  lazy val responseContentType: ContentType = ContentTypes.`application/json`

  /**
   * Convenience method to return an exception as a 500 Internal Error with the body being the message
   * of the exception
   */
  def errorResponse(e: Exception): HttpResponse = {
    HttpResponse(InternalServerError, e.getMessage)
  }

  /**
   * Utility method to return HttpResponse with status OK and a serialized json body via Jackson.
   * If the JSON processing is CPU intensive, it should be done in a background thread, dedicated actor, etc...
   * into an http body with the content type set
   * @param t the object to serialize
   * @tparam T the type to serialize from
   * @return an HttpResponse containing an OK StatusCode and the serialized object
   */
  def jsonOKResponse[T : Manifest](t: T): HttpResponse = {
    // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
    HttpResponse(OK, toJsonBody(t))
  }

  /**
   * Utility method to serialize a json body via jackson.
   * If the JSON processing is CPU intensive, it should be done in a background thread, dedicated actor, etc...
   * into an http body with the content type set
   * @param t the object to serialize
   * @tparam T the type to serialize from
   * @return an HttpResponse containing either the desired HttpEntity, or an error entity
   */
  def toJsonBody[T : Manifest](t: T): HttpEntity = {
    // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
    JsonUtil.toJson(t) match {
      case Success(j) => HttpEntity(responseContentType, j)
      case Failure(e) => coerceError(Option(e.getMessage).getOrElse("").getBytes(charsetUtf8))
    }
  }

  /**
   * Used under the covers to force simple error strings into a JSON format
   * @param body the body
   * @return an HttpEntity containing an error JSON body
   */
  def coerceError(body: Array[Byte]): HttpEntity = {
    toJsonBody(Map("errors" -> List(new String(body, charsetUtf8))))
  }

}
