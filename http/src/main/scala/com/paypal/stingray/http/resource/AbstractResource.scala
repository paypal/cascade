package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.HttpMethods._
import spray.http.HttpResponse
import StatusCodes._
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.json._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.concurrent._
import scala.util.{Success => TrySuccess, Failure => TryFailure}
import org.slf4j.LoggerFactory

/**
 * Base class for HTTP resources built with Spray.
 *
 * @tparam ParsedRequest A representation of the request as this resource sees it. This should contain all the data
 *                       from the request needed by this resource to produce the response (except the body).
 *                       Use the type [[spray.http.HttpRequest]] and
 *                       trait [[com.paypal.stingray.http.resource.NoParsing]] to skip parsing
 * @tparam AuthInfo a structure for information gained during authorization.
 *                  Use the type [[com.paypal.stingray.http.resource.NoAuth]]
 *                  and trait [[com.paypal.stingray.http.resource.AlwaysAuthorized]] to skip authorization
 * @tparam PostBody the class to serialize the POST body to. Use the type [[com.paypal.stingray.http.resource.NoBody]]
 *                  if the resource doesn't do POST, or doesn't use a body
 * @tparam PutBody the class to serialize the PUT body to. Use the type [[com.paypal.stingray.http.resource.NoBody]]
 *                 if the resource doesn't do PUT, or doesn't use a body
 */
abstract class AbstractResource[ParsedRequest, AuthInfo, PostBody, PutBody] extends LoggingSugar {

  protected lazy val logger = LoggerFactory.getLogger(this.getClass)

  /** Default context used by futures created in this resource */
  lazy val executionContext: ExecutionContext = new ExecutionContext {
    def reportFailure(t: Throwable) {
      logger.warn(t.getMessage, t)
    }

    def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  /** Provide your own, or the default will be used */
  implicit lazy val context: ExecutionContext = executionContext

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
   * @param r the request to parse into some data structure
   * @return the parsed request, or a Failure response
   */
  def parseRequest(r: HttpRequest, pathParts: Map[String, String]): Future[ParsedRequest]

  /**
   * Convert the raw body into the PostBody type
   * @param r the HTTP request
   * @return optionally, an object of the PostBody type
   */
  def parsePostBody(r: HttpRequest): Future[Option[PostBody]] = none[PostBody].continue

  /**
   * Convert the raw body into the PutBody type
   * @param r the http request
   * @return optionally, an object of the PutBody type
   */
  def parsePutBody(r: HttpRequest): Future[Option[PutBody]] = none[PutBody].continue

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
   * @param p the parsed request
   * @return optionally, the AuthInfo for this request, or a Failure(halt)
   */
  def isAuthorized(p: ParsedRequest): Future[Option[AuthInfo]]

  /**
   * Whether an incoming request is forbidden to execute, by default false
   * @param p the parsed request
   * @return Failure(halt) if forbidden, false if not
   */
  def isForbidden(p: ParsedRequest): Future[Boolean] = false.continue
  def isForbidden(p: ParsedRequest, auth: AuthInfo): Future[Boolean] = isForbidden(p)

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
   * Handle a GET request.
   * @return a response for the given request
   */
  def doGet(req: ParsedRequest): Future[HttpResponse] = HttpResponse(InternalServerError).continue
  def doGet(req: ParsedRequest, authInfo: AuthInfo): Future[HttpResponse] = doGet(req)

  /**
   * Handle a HEAD request. By default it routes to get and strips any body
   * @return a response for the given request
   */
  def doHead(req: ParsedRequest): Future[HttpResponse] = doGet(req).map { resp =>
    resp.withEntity(Empty)
  }
  def doHead(req: ParsedRequest, authInfo: AuthInfo): Future[HttpResponse] = doGet(req)

  /**
   * Handle a DELETE request
   * @return the response for the delete
   */
  def doDelete(req: ParsedRequest): Future[HttpResponse] = HttpResponse(InternalServerError).continue
  def doDelete(req: ParsedRequest, authInfo: AuthInfo): Future[HttpResponse] = doDelete(req)

  /**
   * Handle a POST request. Use either this or `doPostAsCreate`, but not both.
   *@return the response for the post
   */
  def doPost(req: ParsedRequest, body: PostBody): Future[HttpResponse] = HttpResponse(InternalServerError).continue
  def doPost(req: ParsedRequest, authInfo: AuthInfo, body: PostBody): Future[HttpResponse] = doPost(req, body)

  /**
   * Handle a POST request, treating it as a create in the CRUD paradigm. The path of the newly created resource
   * should be returned along with the response, if applicable, and it will be incorporated into the location header
   * if a 201 is returned. Use either this or `doPost`, but not both.
   *@return the response for the post and the new location
   */
  def doPostAsCreate(req: ParsedRequest, authInfo: AuthInfo, body: PostBody): Future[(HttpResponse, Option[String])] = {
    doPost(req, authInfo, body).map { resp =>
      resp -> none[String]
    }
  }

  /**
   * Handle a PUT request
   * @return the response for the put
   */
  def doPut(req: ParsedRequest, b: PutBody): Future[HttpResponse] = HttpResponse(InternalServerError).continue
  def doPut(req: ParsedRequest, authInfo: AuthInfo, b: PutBody): Future[HttpResponse] = doPut(req, b)

  /**
   * Handle an OPTIONS request
   * @return the response
   */
  def doOptions(req: ParsedRequest): Future[HttpResponse] = HttpResponse(InternalServerError).continue
  def doOptions(req: ParsedRequest, authInfo: AuthInfo): Future[HttpResponse] = doOptions(req)

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
   * Utility method to serialize a json body via lift-json-scalaz.
   * If the JSON processing is CPU intensive, it should be done in a background thread, dedicated actor, etc...
   * into an http body with the content type set
   * @param t the object to serialize
   * @tparam T the type to serialize from
   * @return an HttpResponse containing either the desired HttpEntity, or an error entity
   */
  def toJsonBody[T : Manifest](t: T): HttpEntity = {
    // TODO: convert Manifest patterns to use TypeTag, ClassTag when Jackson implements that
    JsonUtil.toJson(t) match {
      case TrySuccess(j) => HttpEntity(responseContentType, j)
      case TryFailure(e) => coerceError(e.getMessage.getBytes(charsetUtf8))
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
