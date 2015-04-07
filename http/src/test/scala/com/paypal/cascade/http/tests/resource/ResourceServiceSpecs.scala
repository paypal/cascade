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
package com.paypal.cascade.http.tests.resource

import akka.actor.ActorSelection
import akka.testkit.TestActorRef
import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.http.resource.{ResourceDriver, HttpResourceActor}
import com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext
import com.paypal.cascade.http.resource.ResourceDriver._
import com.paypal.cascade.http.server.SprayConfiguration
import com.paypal.cascade.http.tests.resource.DummyResource.GetRequest
import org.specs2._
import com.paypal.cascade.http.tests.api.SprayRoutingServer
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import spray.http.{HttpRequest, StatusCodes, HttpMethods}
import spray.http.HttpHeaders.RawHeader
import com.paypal.cascade.json._
import spray.routing.Directives._

import scala.util.Try

/**
 * Tests for [[com.paypal.cascade.http.resource.ResourceService]]
 */
class ResourceServiceSpecs extends SpecificationLike with ScalaCheck { def is = args(sequential = true) ^ s2"""

  Status endpoint should return proper build info keys                   ${Status().ok}
  Stats endpoint should return proper stats info                         ${Stats().ok}
  Stats endpoint should return failure without a proper server actor     ${Stats().fails}

"""

  private def createDummyResource(ctx: ResourceContext): DummyResource = {
    new DummyResource(ctx)
  }

  private val parseRequest: HttpResourceActor.RequestParser = { _ : HttpRequest =>
    Try (GetRequest("bar"))
  }

  private val rewriteRequest: RewriteFunction[GetRequest] = { req: HttpRequest =>
    Try((req,GetRequest("bar")))
  }

  private lazy val serviceName = "dummyResourceService"

  private lazy val systemWrapper = new ActorSystemWrapper(serviceName)

  private lazy val config = SprayConfiguration(serviceName, 8080, 15) {
    path("ping") {
      get {
        ResourceDriver.serve(createDummyResource, parseRequest)(systemWrapper.actorRefFactory)
      }
    } ~
    path("ping-rewrite") {
      get {
        ResourceDriver.serveWithRewrite(createDummyResource)(rewriteRequest)(systemWrapper.actorRefFactory)
      }
    }
  }

  class Context extends CommonImmutableSpecificationContext {
    protected implicit lazy val system = systemWrapper.system
    protected lazy val statsSel = ActorSelection(TestActorRef(new StatsActor), Nil)
    protected lazy val devNullSel = ActorSelection(TestActorRef(new DevNullActor), Nil)
  }

  case class Status() extends Context {
    def ok = apply {
      lazy val sprayRoutingServer = TestActorRef(
        new SprayRoutingServer(config, systemWrapper, devNullSel)
      )

      val matcherKeys = Seq("status", "service-name", "dependencies", "git-info")
      val res = sprayRoutingServer.underlyingActor.makeRequest(HttpMethods.GET,
        "/status",
        List(RawHeader("x-service-status", "true")),
        None)

      val success = res.status must beEqualTo(StatusCodes.OK)
      val mappedData = res.entity.data.asString.fromJson[Map[String, Any]].get
      val keys = mappedData.map { item =>
        val (k, _) = item
        k
      }
      val entityMatches = keys must containTheSameElementsAs(matcherKeys)
      success and entityMatches
    }
  }

  case class Stats() extends Context {
    def ok = apply {
      lazy val sprayRoutingServer = TestActorRef(new SprayRoutingServer(config, systemWrapper, statsSel))
      val res = sprayRoutingServer.underlyingActor.makeRequest(HttpMethods.GET,
        "/stats",
        List(RawHeader("x-service-stats", "true")),
        None)
      val correctStatus = res.status must beEqualTo(StatusCodes.OK)
      val mappedData = res.entity.data.asString.fromJson[Map[String, Any]].get
      val values = mappedData.map { item =>
        val (_, v) = item
        v
      }
      val dataMatches = values.map { statsValue =>
        statsValue must beEqualTo(1)
      }.reduceLeft { (first, second) =>
        first and second
      }
      correctStatus and dataMatches
    }

    def fails = apply {
      lazy val sprayRoutingServer = TestActorRef(new SprayRoutingServer(config, systemWrapper, devNullSel))
      val res = sprayRoutingServer.underlyingActor.makeRequest(HttpMethods.GET,
        "/stats",
        List(RawHeader("x-service-stats", "true")),
        None)
      // server actor not started, so this throws a 500
      val success = res.status must beEqualTo(StatusCodes.InternalServerError)
      success
    }
  }
}
