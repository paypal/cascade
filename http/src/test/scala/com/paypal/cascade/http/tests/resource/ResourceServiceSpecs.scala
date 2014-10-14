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

import akka.testkit.TestActorRef
import com.paypal.cascade.akka.actor.ActorSystemWrapper
import com.paypal.cascade.http.resource.{ResourceDriver, HttpResourceActor}
import com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext
import com.paypal.cascade.http.resource.ResourceDriver._
import com.paypal.cascade.http.server.SprayConfiguration
import com.paypal.cascade.http.tests.resource.DummyResource.GetRequest
import org.specs2._
import com.paypal.cascade.http.tests.api.SprayRoutingClient
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import spray.http.{HttpRequest, StatusCodes, HttpMethods}
import spray.http.HttpHeaders.RawHeader
import com.paypal.cascade.json._
import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import spray.can.server.{Stats => SprayStats}
import spray.can.Http.GetStats
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

  private lazy val actorSystemWrapper = new ActorSystemWrapper(serviceName)

  private lazy val config = SprayConfiguration(serviceName, 8080, 15) {
    path("ping") {
      get {
        ResourceDriver.serve(createDummyResource, parseRequest)(actorSystemWrapper.actorRefFactory)
      }
    } ~
    path("ping-rewrite") {
      get {
        ResourceDriver.serveWithRewrite(createDummyResource)(rewriteRequest)(actorSystemWrapper.actorRefFactory)
      }
    }
  }

  val statsObject = SprayStats(new FiniteDuration(1L, TimeUnit.SECONDS), 1L, 1L, 1L, 1L, 1L, 1L, 1L)

  class Context extends CommonImmutableSpecificationContext {
    protected lazy val sprayRoutingClient = new SprayRoutingClient(config, actorSystemWrapper)
  }

  case class Status() extends Context {
    def ok = apply {
      ParentActor.withRunning(statsObject, actorSystemWrapper) {
        val matcherKeys = Seq("status", "service-name", "dependencies", "git-info")
        val res = sprayRoutingClient.makeRequest(HttpMethods.GET,
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
  }

  case class Stats() extends Context {
    def ok = apply {
      ParentActor.withRunning(statsObject, actorSystemWrapper) {
        val res = sprayRoutingClient.makeRequest(HttpMethods.GET,
          "/stats",
          List(RawHeader("x-service-stats", "true")),
          None)
        val correctStatus = res.status must beEqualTo(StatusCodes.OK)
        val mappedData = res.entity.data.asString.fromJson[Map[String, Any]].get
        val values = mappedData.map { item =>
          val (_, v) = item
          v
        }
        val dataMatches = values.map {
          case dur: Map[_, _] => dur must beEqualTo(Map("finite" -> true))
          case other => other must beEqualTo(1)
        }.reduceLeft { (first, second) =>
          first and second
        }
        correctStatus and dataMatches
      }
    }

    def fails = apply {
      val res = sprayRoutingClient.makeRequest(HttpMethods.GET,
        "/stats",
        List(RawHeader("x-service-stats", "true")),
        None)
      // server actor not started, so this throws a 500
      val success = res.status must beEqualTo(StatusCodes.InternalServerError)
      success
    }
  }
}

class ParentActor(statsObject: SprayStats) extends Actor {
  def receive: Receive = {
    case _ =>
  }
  context.actorOf(Props(new ServerActor(statsObject)), "listener-0")
}

object ParentActor {
  def withRunning[T](statsObject: SprayStats, systemWrapper: ActorSystemWrapper)(fn: => T): T = {
    implicit val actorSystem = systemWrapper.system
    val ref = actorSystem.actorOf(Props(new ParentActor(statsObject)), "IO-HTTP")
    try {
      fn
    } finally {
      systemWrapper.system.stop(ref)
    }
  }
}

class ServerActor(statsObject: SprayStats) extends Actor {
  def receive: Receive = {
    case GetStats => sender ! statsObject
  }
}

