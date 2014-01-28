package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http.HttpHeaders._
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.concurrent.Future

/**
 * Implementation of a basic HTTP request handling pipeline.
 *
 * Used in [[com.paypal.stingray.http.resource.ResourceService]] to push along HTTP requests. This trait should not
 * normally be directly extended, but rather will be part of an extended ResourceService.
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 */

trait ResourceDriver extends LoggingSugar {

  protected lazy val logger = getLogger[ResourceDriver]

  /**
   * Continues execution if this resource is available, or halts
   * @param resource this resource
   * @return an empty Future
   */
  def ensureAvailable(resource: AbstractResource[_, _, _, _]): Future[Unit] = {
    import resource.context
    resource.available.orHaltWith(ServiceUnavailable)
  }

  /**
   * Continues execution if this method is supported, or halts
   * @param resource this resource
   * @param method the method sent
   * @return an empty Future
   */
  def ensureMethodSupported(resource: AbstractResource[_, _, _, _],
                            method: HttpMethod): Future[Unit] = {
    import resource.context
    resource.supportedHttpMethods.contains(method).orHaltWith(MethodNotAllowed)
  }

  /**
   * Attempts to parse this request body, if one exists
   * @param request the request
   * @param method the method sent
   * @param f a function to parse this request body
   * @tparam T the `ParsedRequest` type
   * @return a Future with an optional parsed body, or None if parsing fails
   */
  def parseBody[T](request: HttpRequest, method: HttpMethod)
                  (f: HttpRequest => Future[Option[T]]): Future[Option[T]] = {
    if(request.method == method) {
      f(request)
    } else {
      none[T].continue
    }
  }

  /**
   * Continues execution and yields an `AuthInfo` if this method is authorized, or halts
   * @param resource this resource
   * @param parsedRequest the request after parsing
   * @tparam PR the `ParsedRequest` type
   * @tparam AI the `AuthInfo` type
   * @return a Future containing an `AuthInfo` object, or a failure
   */
  def ensureAuthorized[PR, AI](resource: AbstractResource[PR, AI, _, _],
                               parsedRequest: PR): Future[AI] = {
    import resource.context
    for {
      authInfoOpt <- resource.isAuthorized(parsedRequest)
      authInfo <- authInfoOpt.orHaltWith(Unauthorized)
    } yield authInfo
  }

  /**
   * Continues execution if this resource is not forbidden to the requester, or halts
   * @param resource this resource
   * @param parsedRequest the request after parsing
   * @param authInfo the `AuthInfo` after authorization
   * @tparam PR the `ParsedRequest` type
   * @tparam AI the `AuthInfo` theype
   * @return an empty Future
   */
  def ensureNotForbidden[PR, AI](resource: AbstractResource[PR, AI, _, _],
                                 parsedRequest: PR,
                                 authInfo: AI): Future[Unit] = {
    import resource.context
    for {
      isForbidden <- resource.isForbidden(parsedRequest, authInfo)
      _ <- (!isForbidden).orHaltWith(Forbidden)
    } yield ()
  }

  /**
   * Continues execution if this resource supports the content type sent in the request, or halts
   * @param resource this resource
   * @param request the request
   * @return an empty Future
   */
  def ensureContentTypeSupported(resource: AbstractResource[_, _, _, _],
                                 request: HttpRequest): Future[Unit] = {
    request.entity match {
      case Empty => ().continue
      case NonEmpty(ct, _) => resource.acceptableContentTypes.contains(ct).orHaltWith(UnsupportedMediaType)
    }
  }

  /**
   * Continues execution if this resource can respond in a format that the requester can accept, or halts
   * @param resource this resource
   * @param request the request
   * @return a Future containing the acceptable content type found, or a failure
   */
  def ensureResponseContentTypeAcceptable(resource: AbstractResource[_, _, _, _],
                                          request: HttpRequest): Future[ContentType] = {
    import resource.context
    request.acceptableContentType(List(resource.responseContentType)).orHaltWith(NotAcceptable)
  }

  /**
   * Given a matching HTTP response code, add the given header to that response
   * @param response the initial response
   * @param status the response status code
   * @param header the header to conditionally add
   * @return a possibly modified response
   */
  def addHeaderOnCode(response: HttpResponse, status: StatusCode)
                     (header: => HttpHeader): HttpResponse = {
    if(response.status == status) {
      response.withHeaders(header :: response.headers)
    } else {
      response
    }

  }

  /**
   * Main driver for HTTP requests
   * @param request the incoming request
   * @param resource this resource
   * @param pathParts the parsed path
   * @tparam ParsedRequest the request after parsing
   * @tparam AuthInfo the authorization container
   * @tparam PostBody the POST body after parsing
   * @tparam PutBody the PUT body after parsing
   * @return a Future containing an HttpResponse
   */
  def serveSync[ParsedRequest, AuthInfo, PostBody, PutBody](request: HttpRequest,
                                                            resource: AbstractResource[ParsedRequest, AuthInfo, PostBody, PutBody],
                                                            pathParts: Map[String, String]): Future[HttpResponse] = {

    import resource.context

    (for {
      _ <- ensureAvailable(resource)
      _ <- ensureMethodSupported(resource, request.method)

      parsedRequest <- resource.parseRequest(request, pathParts)
      postBody <- parseBody(request, POST)(resource.parsePostBody)
      putBody <- parseBody(request, PUT)(resource.parsePutBody)

      authInfo <- ensureAuthorized(resource, parsedRequest)
      _ <- ensureNotForbidden(resource, parsedRequest, authInfo)
      _ <- ensureContentTypeSupported(resource, request)
      _ <- ensureResponseContentTypeAcceptable(resource, request)

      (httpResponse, location) <- request.method match {
        case GET => resource.doGet(parsedRequest, authInfo).map((_, none[String]))
        case HEAD => resource.doHead(parsedRequest, authInfo).map((_, none[String]))
        case PUT => {
          putBody.orError().flatMap { body =>
            resource.doPut(parsedRequest, authInfo, body).map((_, none[String]))
          }
        }
        case POST => {
          postBody.orError().flatMap { body =>
            resource.doPostAsCreate(parsedRequest, authInfo, body)
          }
        }
        case DELETE => resource.doDelete(parsedRequest, authInfo).map((_, none[String]))
        case OPTIONS => resource.doOptions(parsedRequest, authInfo).map((_, none[String]))
        case _ => halt(MethodNotAllowed)
      }
    } yield {
      val responseWithLocation = addHeaderOnCode(httpResponse, Created) {
        val scheme = request.headers.find(_.name == "X-Forwarded-Proto").map(_.value).getOrElse("http")
        // TODO: there is probably a better way to do this using Spray itself
        Location(Uri(s"$scheme://${request.uri.authority.host}:${request.uri.effectivePort}${request.uri.path}${location.map("/" + _).getOrElse("")}"))
      }
      // Just force the request to the right content type
      responseWithLocation.withEntity(responseWithLocation.entity.flatMap((entity: NonEmpty) => HttpEntity(resource.responseContentType, entity.data)))
    }).recover {
      case e: HaltException =>
        val response = addHeaderOnCode(e.response, Unauthorized) {
          `WWW-Authenticate`(resource.unauthorizedChallenge(request))
        }
        // If the error already has the right content type, let it through, otherwise coerce it
        val finalResponse = response.withEntity(response.entity.flatMap { entity: NonEmpty =>
          entity.contentType match {
            case resource.responseContentType => entity
            case _ => resource.coerceError(entity.data.toByteArray)
          }
        })
        if (finalResponse.status.intValue >= 500) {
          logger.warn(s"Request finished unsuccessfully: request: $request response: $finalResponse")
        }
        finalResponse
      case t: Throwable => {
        logger.error(s"Unexpected error: request: $request error: ${t.getMessage}", t)
        HttpResponse(InternalServerError, resource.coerceError(Option(t.getMessage).getOrElse("").getBytes(charsetUtf8)))
      }

    }

  }

}
