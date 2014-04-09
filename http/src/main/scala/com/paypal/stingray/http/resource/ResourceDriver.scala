package com.paypal.stingray.http.resource

import spray.http._
import spray.http.HttpEntity._
import spray.http.StatusCodes.{Success => _, _}
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.concurrent.{ExecutionContext, Future}
import scala.util._
import spray.routing.RequestContext
import akka.actor.{ActorRef, ActorRefFactory}

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
                           request: HttpRequest)
                          (implicit ctx: ExecutionContext): Future[AI] = {
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
   * Run the request on this resource, first applying a rewrite. This should not be overridden.
   * @param resource this resource
   * @param rewrite a method by which to rewrite the request
   * @tparam ParsedRequest the request after parsing
   * @tparam AuthInfo the authorization container
   * @return the rewritten request execution
   */
  final def serveWithRewrite[ParsedRequest, AuthInfo](resource: AbstractResource[AuthInfo],
                                                      processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                      mbResponseActor: Option[ActorRef] = None)
                                                     (rewrite: HttpRequest => Try[(HttpRequest, ParsedRequest)])
                                                     (implicit actorRefFactory: ActorRefFactory): RequestContext => Unit = {
    ctx: RequestContext =>
      rewrite(ctx.request).map {
        case (request, parsed) =>
          val serveFn = serve(resource, processFunction, r => Success(parsed))
          serveFn(ctx.copy(request = request))
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
                                           requestParser: HttpRequest => Try[ParsedRequest],
                                           mbResponseActor: Option[ActorRef] = None)
                                          (implicit actorRefFactory: ActorRefFactory): RequestContext => Unit = {
    ctx: RequestContext => {
      val actor = actorRefFactory.actorOf(ResourceActor.props(resource, ctx, requestParser, processFunction, mbResponseActor))
      actor ! ResourceActor.Start
    }
  }
}
