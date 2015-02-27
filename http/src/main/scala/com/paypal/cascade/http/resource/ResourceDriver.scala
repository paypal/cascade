/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.http.resource

import scala.concurrent.duration.FiniteDuration
import scala.util._

import akka.actor.{ActorRef, ActorRefFactory}
import spray.http.StatusCodes.{Success => _, _}
import spray.http._
import spray.routing.RequestContext

import com.paypal.cascade.http.resource.HttpResourceActor.{RequestParser, ResourceContext}
import com.paypal.cascade.http.util.HttpUtil

/**
 * Implementation of a basic HTTP request handling pipeline. Used to push along HTTP requests.
 */

object ResourceDriver {

  type RewriteFunction[ParsedRequest] = HttpRequest => Try[(HttpRequest, ParsedRequest)]

  /**
   * Run the request on this resource, first applying a rewrite. This should not be overridden.
   * @param resourceActor function for creating the actorRef which will process the request
   * @param mbResponseActor optional actor to send the response or failure
   * @param resourceTimeout time until resource times out. Not the same as spray's timeout, since this can be specified
   *                        per-request.
   * @param rewrite a method by which to rewrite the request
   * @tparam ParsedRequest the request after parsing
   * @return the rewritten request execution
   */
  final def serveWithRewrite[ParsedRequest <: AnyRef](resourceActor: ResourceContext => AbstractResourceActor,
                                            mbResponseActor: Option[ActorRef] = None,
                                            resourceTimeout: FiniteDuration = HttpResourceActor.defaultResourceTimeout)
                                           (rewrite: RewriteFunction[ParsedRequest])
                                           (implicit actorRefFactory: ActorRefFactory): RequestContext => Unit = {
    ctx: RequestContext =>
      rewrite(ctx.request).map {
        case (request, parsed) =>
          val serveFn = serve(resourceActor, (r => Success(parsed): Try[AnyRef]): RequestParser, mbResponseActor, resourceTimeout)
          serveFn(ctx.copy(request = request))
      }.recover {
        case e: Exception =>
          ctx.complete(HttpResponse(InternalServerError, HttpUtil.toJsonBody(s"Error in request execution: ${e.getClass.getSimpleName}")))
      }
  }

  /**
   * Run the request on this resource
   * @param resourceActor function for creating the actorRef which will process the request
   * @param requestParser parser function to parse request into scala data type
   * @param mbResponseActor optional actor to send the response or failure
   * @param resourceTimeout time until resource times out. Not the same as spray's timeout, since this can be specified
   *                        per-request.
   * @return the request execution
   */
  final def serve(resourceActor: ResourceContext => AbstractResourceActor,
                  requestParser: HttpResourceActor.RequestParser,
                  mbResponseActor: Option[ActorRef] = None,
                  resourceTimeout: FiniteDuration = HttpResourceActor.defaultResourceTimeout)
                 (implicit actorRefFactory: ActorRefFactory): RequestContext => Unit = {
    { ctx: RequestContext =>
      val actor = actorRefFactory.actorOf(HttpResourceActor.props(resourceActor, ctx, requestParser, mbResponseActor, resourceTimeout))
      actor ! HttpResourceActor.Start
    }
  }
}
