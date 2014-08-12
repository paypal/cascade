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
package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.common.service.ServiceNameComponent
import spray.routing.Directives._
import com.paypal.stingray.http.resource.{HttpResourceActor, ResourceServiceComponent, ResourceDriver}
import spray.http.HttpRequest
import scala.util.Try
import com.paypal.stingray.akka.actor.ActorSystemComponent
import com.paypal.stingray.http.tests.resource.DummyResource.GetRequest
import com.paypal.stingray.http.resource.HttpResourceActor.ResourceContext
import com.paypal.stingray.http.resource.ResourceDriver.RewriteFunction

/**
 * A dummy resource service implementation for use with [[com.paypal.stingray.http.tests.resource.DummyResource]].
 * Only accepts requests to the "/ping" endpoint.
 */
trait DummyResourceService
  extends ServiceNameComponent
  with ResourceServiceComponent
  with ActorSystemComponent {

  /** This resource */
  val dummy: ResourceContext => DummyResource = new DummyResource(_)

  val parseRequest: HttpResourceActor.RequestParser = { _ : HttpRequest =>
    Try (GetRequest("bar"))
  }

  val rewriteRequest: RewriteFunction[GetRequest] = { req: HttpRequest =>
    Try((req,GetRequest("bar")))
  }

  /** The route for this resource */
  override val route = {
    path("ping") {
      get {
        ResourceDriver.serve(dummy, parseRequest)
      }
    } ~
    path("ping-rewrite") {
      get {
        ResourceDriver.serveWithRewrite(dummy)(rewriteRequest)
      }
    }
  }

  override lazy val serviceName = "tests"

}
