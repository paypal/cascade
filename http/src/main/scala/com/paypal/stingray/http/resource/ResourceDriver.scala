package com.paypal.stingray.http.resource

import spray.http._
import spray.http.StatusCodes.{Success => _, _}
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import scala.util._
import spray.routing.RequestContext
import com.paypal.stingray.http.util.HttpUtil
import akka.actor.{ActorRef, ActorRefFactory}
import scala.concurrent.duration.Duration

/**
 * Implementation of a basic HTTP request handling pipeline.
 *
 * Used to push along HTTP requests
 *
 * See https://confluence.paypal.com/cnfl/display/stingray/AbstractResource%2C+ResourceDriver%2C+and+ResourceService
 * for more information.
 */

object ResourceDriver {

  type RewriteFunction[ParsedRequest] = HttpRequest => Try[(HttpRequest, ParsedRequest)]

  /**
   * Run the request on this resource, first applying a rewrite. This should not be overridden.
   * @param resourceActor function for creating the actorRef which will process the request
   * @param rewrite a method by which to rewrite the request
   * @tparam ParsedRequest the request after parsing
   * @return the rewritten request execution
   */
  final def serveWithRewrite[ParsedRequest](resourceActor: ActorRef => AbstractResourceActor,
                                            mbResponseActor: Option[ActorRef] = None,
                                            recvTimeout: Duration = ResourceHttpActor.defaultRecvTimeout,
                                            processRecvTimeout: Duration = ResourceHttpActor.defaultProcessRecvTimeout)
                                           (rewrite: RewriteFunction[ParsedRequest])
                                           (implicit actorRefFactory: ActorRefFactory): RequestContext => Unit = {
    ctx: RequestContext =>
      rewrite(ctx.request).map {
        case (request, parsed) =>
          val serveFn = serve(resourceActor, r => Success(parsed), mbResponseActor, recvTimeout, processRecvTimeout)
          serveFn(ctx.copy(request = request))
      }.recover {
        case e: Exception =>
          ctx.complete(HttpResponse(InternalServerError, HttpUtil.coerceError(Option(e.getMessage).getOrElse("").getBytes(charsetUtf8))))
      }
  }

  /**
   * Run the request on this resource
   * @param resourceActor function for creating the actorRef which will process the request
   * @tparam ParsedRequest the request after parsing
   * @return the request execution
   */
  final def serve[ParsedRequest](resourceActor: ActorRef => AbstractResourceActor,
                                 requestParser: ResourceHttpActor.RequestParser[ParsedRequest],
                                 mbResponseActor: Option[ActorRef] = None,
                                 recvTimeout: Duration = ResourceHttpActor.defaultRecvTimeout,
                                 processRecvTimeout: Duration = ResourceHttpActor.defaultProcessRecvTimeout)
                                (implicit actorRefFactory: ActorRefFactory): RequestContext => Unit = {
    { ctx: RequestContext =>
      val actor = actorRefFactory.actorOf(ResourceHttpActor.props(resourceActor, ctx, requestParser, mbResponseActor, recvTimeout, processRecvTimeout))
      actor ! ResourceHttpActor.Start
    }
  }
}
