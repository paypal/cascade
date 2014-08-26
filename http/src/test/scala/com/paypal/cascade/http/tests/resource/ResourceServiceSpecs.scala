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

import org.specs2._
import com.paypal.cascade.http.tests.api.SprayRoutingClientComponent
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import spray.http.{StatusCodes, HttpMethods}
import spray.http.HttpHeaders.RawHeader
import com.paypal.cascade.json._
import akka.actor.{ActorSystem, Props, Actor}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import spray.can.server.{Stats => SprayStats}
import spray.can.Http.GetStats

/**
 * Tests for [[com.paypal.cascade.http.resource.ResourceServiceComponent.ResourceService]]
 */
class ResourceServiceSpecs extends SpecificationLike with ScalaCheck { def is=s2"""

  Status endpoint should return proper build info keys                   ${Status().ok}
  Stats endpoint should return proper stats info                         ${Stats().ok}
  Stats endpoint should return failure without a proper server actor     ${Stats().fails}

"""

  val statsObject = SprayStats(new FiniteDuration(1L, TimeUnit.SECONDS), 1L, 1L, 1L, 1L, 1L, 1L, 1L)

  class ParentActor extends Actor {
    def receive = {
      case _ =>
    }
    context.actorOf(Props(new ServerActor), "listener-0")
  }
  class ServerActor extends Actor {
    def receive = {
      case GetStats => sender ! statsObject
    }
  }

  class TestEnv extends DummyResourceService with SprayRoutingClientComponent {
    override implicit lazy val system = ActorSystem()
    system.actorOf(Props(new ParentActor), "IO-HTTP")
  }

  class FailTestEnv extends DummyResourceService with SprayRoutingClientComponent

  class Context extends CommonImmutableSpecificationContext {
    val env = new TestEnv
    val failEnv = new FailTestEnv
  }

  case class Status() extends Context {
    def ok = {
      val matcherKeys = Seq("status", "service-name", "dependencies", "git-info")
      val res = env.sprayRoutingClient.makeRequest(HttpMethods.GET, "/status", List(RawHeader("x-service-status", "true")), None)
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
    def ok = {
      val res = env.sprayRoutingClient.makeRequest(HttpMethods.GET, "/stats", List(RawHeader("x-service-stats", "true")), None)
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
    def fails = {
      val res = failEnv.sprayRoutingClient.makeRequest(HttpMethods.GET, "/stats", List(RawHeader("x-service-stats", "true")), None)
      // server actor not started, so this throws a 500
      val success = res.status must beEqualTo(StatusCodes.InternalServerError)
      success
    }
  }
}
