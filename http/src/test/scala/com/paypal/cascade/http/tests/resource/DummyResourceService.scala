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
package com.paypal.cascade.http.tests.resource

import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.http.server.SprayConfiguration
import spray.routing.Directives._
import com.paypal.cascade.http.resource.{ResourceService, HttpResourceActor, ResourceDriver}
import spray.http.HttpRequest
import scala.util.Try
import com.paypal.cascade.http.tests.resource.DummyResource.GetRequest
import com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext
import com.paypal.cascade.http.resource.ResourceDriver.RewriteFunction

/**
 * A dummy resource service implementation for use with [[com.paypal.cascade.http.tests.resource.DummyResource]].
 * Only accepts requests to the "/ping" endpoint.
 */
class DummyResourceService extends ResourceService {

  def dummyResource(ctx: ResourceContext): DummyResource = {
    new DummyResource(ctx)
  }

  val parseRequest: HttpResourceActor.RequestParser = { _ : HttpRequest =>
    Try (GetRequest("bar"))
  }

  val rewriteRequest: RewriteFunction[GetRequest] = { req: HttpRequest =>
    Try((req,GetRequest("bar")))
  }

  private val serviceName = "dummyResourceService"

  override lazy val config = SprayConfiguration(serviceName, 8080, 15) {
    path("ping") {
      get {
        ResourceDriver.serve(dummyResource, parseRequest)
      }
    } ~
    path("ping-rewrite") {
      get {
        ResourceDriver.serveWithRewrite(dummyResource)(rewriteRequest)
      }
    }
  }

  override lazy val actorSystemWrapper = new ActorSystemWrapper(serviceName)
}
