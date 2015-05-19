/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.examples.http.resource

import com.paypal.cascade.http.resource.AbstractResourceActor
import com.paypal.cascade.http.resource.HttpResourceActor.{ProcessRequest, ResourceContext}
import spray.http.{StatusCodes, HttpRequest}
import scala.util.Try

/**
 * MyHttpResource implements the {{{PartialFunction}}} to process incoming HTTP
 * requests.
 * Under the hood, it's a plain Akka actor that adds extra utilities (logging,
 * authorization, parsing, etc...) before and after your {{{resourceReceive}}}
 * functionality executes. When this actor receives a {{{ProcessRequest}}}
 * message, that means that the Content-Type is supported & acceptable, and the
 * request was parsed successfully. After you complete processing, call
 * {{{complete...}}}, or {{{sendError...}}} to continue processing the request.
 * After you do that, the processing will continue with appending appropriate
 * response headers, handling timeouts, returning to spray, etc...
 * @param ctx the resource context. will be passed by {{{ResourceDriver}}}
 */
class MyHttpResource(ctx: ResourceContext) extends AbstractResourceActor(ctx) {
  override def resourceReceive: Receive = {
    case ProcessRequest(req: HttpRequest) => completeToJSON(StatusCodes.OK, "world!")
  }
}

object MyHttpResource {
  /**
   * utility method for creating a new {{{MyHttpResource}}}
   * @param ctx the resource context. will be passed by {{{ResourceDriver}}}
   * @return a new MyHttpResource.
   */
  def apply(ctx: ResourceContext): MyHttpResource = {
    new MyHttpResource(ctx)
  }

  /**
   * the parser for MyHttpResource
   * @param req the request to be parsed
   * @return the result of the parse. {{{Success}}} if it was parsed correctly,
   * {{{Failure}}} otherwise.
   */
  def requestParser(req: HttpRequest): Try[AnyRef] = {
    Try(req)
  }
}
