package com.paypal.stingray.http.resource

import spray.http._
import spray.http.StatusCodes.{Success => _, _}
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.concurrent.Future
import scala.util._
import spray.routing.RequestContext
import akka.actor.{ActorRef, ActorRefFactory}
import com.paypal.stingray.http.util.HttpUtil
import spray.http.HttpHeaders.RawHeader

/**
 * Implementation of a basic HTTP request handling pipeline.
 *
 * Used to push along HTTP requests
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 */

object ResourceDriver {


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
  def serveWithRewrite[ParsedRequest, AuthInfo](resource: AbstractResource[AuthInfo],
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
  def serve[ParsedRequest, AuthInfo](resource: AbstractResource[AuthInfo],
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
