package com.paypal.stingray.http.tests.resource

import org.specs2._
import com.paypal.stingray.http.tests.api.SprayRoutingClientComponent
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import spray.http.{StatusCodes, HttpMethods}
import spray.http.HttpHeaders.RawHeader
import com.paypal.stingray.json._

/**
 * Tests for [[com.paypal.stingray.http.resource.ResourceServiceComponent.ResourceService]]
 *
 * /stats success case is not tested because private server actor cannot start up
 * without the correct environment. Thus we only test the fail case.
 */
class ResourceServiceSpecs extends Specification with ScalaCheck { def is=s2"""

  Status endpoint should return proper build info keys                   ${Status().ok}
  Stats endpoint should return failure without a proper server actor     ${Stats().fails}

"""

  class TestEnv extends DummyResourceService with SprayRoutingClientComponent

  class Context extends CommonImmutableSpecificationContext {
    val env = new TestEnv
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
    def fails = {
      val res = env.sprayRoutingClient.makeRequest(HttpMethods.GET, "/stats", List(RawHeader("x-service-stats", "true")), None)
      // service actor will not start up, so this throws a 500
      val success = res.status must beEqualTo(StatusCodes.InternalServerError)
      success
    }
  }
}
