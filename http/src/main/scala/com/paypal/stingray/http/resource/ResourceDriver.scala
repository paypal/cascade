package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.StatusCodes._
import spray.http.HttpHeaders._
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.concurrent.Future
import spray.http.Uri.Path
import scala.util.{Success, Failure, Try}
import spray.routing.RequestContext
import com.paypal.stingray.http.util.HttpUtil

/**
 * Implementation of a basic HTTP request handling pipeline.
 *
 * Used to push along HTTP requests
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 */

object ResourceDriver extends LoggingSugar {

  protected lazy val logger = getLogger[ResourceDriver.type]

  /**
   * Continues execution if this method is supported, or halts
   * @param resource this resource
   * @param method the method sent
   * @return an empty Try
   */
  def ensureMethodSupported(resource: AbstractResource[_],
                            method: HttpMethod): Try[Unit] = {
    resource.supportedHttpMethods.contains(method).orHaltWithT(MethodNotAllowed)
  }

  /**
   * Attempts to parse this request body, if one exists
   * @param request the request
   * @param method the method sent
   * @param f a function to parse this request body
   * @tparam T the `ParsedRequest` type
   * @return a Try with an optional parsed body, or None if parsing fails
   */
  def parseBody[T](request: HttpRequest, method: HttpMethod)
                  (f: HttpRequest => Try[Option[T]]): Try[Option[T]] = {
    if(request.method == method) {
      f(request)
    } else {
      Success(none[T])
    }
  }

  /**
   * Continues execution and yields an `AuthInfo` if this method is authorized, or halts
   * @param resource this resource
   * @param request the request
   * @tparam AI the `AuthInfo` type
   * @return a Future containing an `AuthInfo` object, or a failure
   */
  def ensureAuthorized[AI](resource: AbstractResource[AI],
                           request: HttpRequest): Future[AI] = {
    import resource.context
    for {
      authInfoOpt <- resource.isAuthorized(request)
      authInfo <- authInfoOpt.orHaltWith(Unauthorized)
    } yield authInfo
  }

  /**
   * Continues execution if this resource supports the content type sent in the request, or halts
   * @param resource this resource
   * @param request the request
   * @return an empty Try
   */
  def ensureContentTypeSupported(resource: AbstractResource[_],
                                 request: HttpRequest): Try[Unit] = {
    request.entity match {
      case Empty => Success()
      case NonEmpty(ct, _) => resource.acceptableContentTypes.contains(ct).orHaltWithT(UnsupportedMediaType)
    }
  }

  /**
   * Continues execution if this resource can respond in a format that the requester can accept, or halts
   * @param resource this resource
   * @param request the request
   * @return a Try containing the acceptable content type found, or a failure
   */
  def ensureResponseContentTypeAcceptable(resource: AbstractResource[_],
                                          request: HttpRequest): Try[ContentType] = {
    request.acceptableContentType(List(resource.responseContentType)).orHaltWithT(NotAcceptable)
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
   * Adds a `Content-Language` header to the current header list if the given `responseLanguage` is not None, and the
   * given `headers` list does not yet have a `Content-Language` header set
   * @param responseLanguage the value to assign the `Content-Language` header, or None, if not required
   * @param headers the current list of headers
   * @return augmented list of `HttpHeader` object, or the same list as `response.headers` if no modifications needed
   */
  private def addLanguageHeader(responseLanguage: Option[Language], headers: List[HttpHeader]) : List[HttpHeader] = {
    responseLanguage match {
      case Some(lang) =>
        if (headers.exists(_.lowercaseName == HttpUtil.CONTENT_LANGUAGE_LC)) {
          headers
        } else {
          RawHeader(HttpUtil.CONTENT_LANGUAGE, lang.toString()) :: headers
        }
      case None => headers
    }
  }

  /**
   * Run the request on this resource, first applying a rewrite. This should not be overridden.
   * @param resource this resource
   * @param rewrite a method by which to rewrite the request
   * @tparam ParsedRequest the request after parsing
   * @tparam AuthInfo the authorization container
   * @return the rewritten request execution
   */
  final def serveWithRewrite[ParsedRequest, AuthInfo](resource: AbstractResource[AuthInfo],
                                                      processFunction: ParsedRequest => Future[(HttpResponse, Option[String])])
                                                     (rewrite: HttpRequest => Try[(HttpRequest, ParsedRequest)]): RequestContext => Unit = {
    ctx: RequestContext =>
      rewrite(ctx.request).map {
        case (request, parsed) =>
          serve(resource, processFunction, r => Success(parsed))(ctx.copy(request = request))
      }.recover {
        case e: Exception =>
          ctx.complete(HttpResponse(InternalServerError, resource.coerceError(Option(e.getMessage).getOrElse("").getBytes(charsetUtf8))))
      }
  }

  /**
   * Run the request on this resource. This should not be overridden.
   * @param resource this resource
   * @param processFunction the function to be executed to process the request
   * @tparam ParsedRequest the request after parsing
   * @tparam AuthInfo the authorization container
   * @return the request execution
   */
  final def serve[ParsedRequest, AuthInfo](resource: AbstractResource[AuthInfo],
                                           processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                           requestParser: HttpRequest => Try[ParsedRequest] = (x: HttpRequest) => Success(())): RequestContext => Unit = {
    ctx: RequestContext => {
      import resource.context
      ctx.complete(serveSync(ctx.request, resource, processFunction, requestParser))
    }
  }


  /**
   * Main driver for HTTP requests
   * @param request the incoming request
   * @param resource this resource
   * @param processFunction the function to be executed to process the request
   * @tparam ParsedRequest the request after parsing
   * @tparam AuthInfo the authorization container
   * @return a Future containing an HttpResponse
   */
  final def serveSync[ParsedRequest, AuthInfo](request: HttpRequest,
                                               resource: AbstractResource[AuthInfo],
                                               processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                               requestParser: HttpRequest => Try[ParsedRequest]): Future[HttpResponse] = {

    def handleError: PartialFunction[Throwable, HttpResponse] = {
      case e: HaltException =>
        val response = addHeaderOnCode(e.response, Unauthorized) {
          `WWW-Authenticate`(resource.unauthorizedChallenge(request))
        }
        val headers = addLanguageHeader(resource.responseLanguage, response.headers)
        // If the error already has the right content type, let it through, otherwise coerce it
        val finalResponse = response.withHeadersAndEntity(headers, response.entity.flatMap { entity: NonEmpty =>
          entity.contentType match {
            case resource.responseContentType => entity
            case _ => resource.coerceError(entity.data.toByteArray)
          }
        })
        if (finalResponse.status.intValue >= 500) {
          logger.warn(s"Request finished unsuccessfully: request: $request response: $finalResponse")
        }
        finalResponse
      case e: Exception =>
        logger.error(s"Unexpected error: request: $request error: ${e.getMessage}", e)
        HttpResponse(InternalServerError,
          resource.coerceError(Option(e.getMessage).getOrElse("").getBytes(charsetUtf8)),
          addLanguageHeader(resource.responseLanguage, Nil))
    }

    import resource.context

    val parsedRequest = for {
      _ <- ensureMethodSupported(resource, request.method)
      parsedReq <- requestParser(request)
      _ <- ensureContentTypeSupported(resource, request)
      _ <- ensureResponseContentTypeAcceptable(resource, request)
    } yield parsedReq

    parsedRequest match {
      case Success(req) =>
        val result = for {
          _ <- ensureAuthorized(resource, request)
          (httpResponse, location) <- processFunction(req)
        } yield {
          val responseWithLocation = addHeaderOnCode(httpResponse, Created) {
            // if an `X-Forwarded-Proto` header exists, read the scheme from that; else, preserve what was given to us
            val newScheme = request.headers.find(_.name == "X-Forwarded-Proto").map(_.value).getOrElse(request.uri.scheme)

            // if we created something, `location` will have more information to append to the response path
            val newPath = Path(request.uri.path.toString + location.map("/" + _).getOrElse(""))

            // copy the request uri, replacing scheme and path as needed, and return a `Location` header with the new uri
            val newUri = request.uri.copy(scheme = newScheme, path = newPath)
            Location(newUri)
          }

          val headers = addLanguageHeader(resource.responseLanguage, responseWithLocation.headers)

          // Just force the request to the right content type
          responseWithLocation.withHeadersAndEntity(headers, responseWithLocation.entity.flatMap((entity: NonEmpty) =>
            HttpEntity(resource.responseContentType, entity.data)))
        }
        result.recover(handleError)
      case Failure(t) =>
        logger.error(s"Unexpected error: request: $request error: ${t.getMessage}", t)
        Future.successful(handleError.apply(t))
    }

  }

}
