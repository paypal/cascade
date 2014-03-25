package com.paypal.stingray.http.tests.server

import org.specs2._
import org.specs2.mock.Mockito
import com.paypal.stingray.http.server.StatusResponse
import com.paypal.stingray.common.properties.BuildProperties
import com.paypal.stingray.common.json.JsonUtil
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.stingray.http.server.StatusResponse]].
 */
class StatusResponseSpecs extends Specification with Mockito { def is = s2"""

  getStatusResponse
    properly creates json response                      ${Response().ok}
    does not fail when some keys are missing            ${Response().okWithMissing}

"""

  trait Context extends CommonImmutableSpecificationContext {
    val bp = mock[BuildProperties]
    bp.get("service.dependencies") returns Option("dep1,dep2,dep3")
    bp.get("git.branch") returns Option("test-branch")
    bp.get("git.branch.clean") returns Option("true")
  }

  case class Response() extends Context {
    def ok = apply {

      bp.get("git.commit.sha") returns Option("1234")
      bp.get("git.commit.date") returns Option("today")

      val resp = StatusResponse.getStatusResponse(bp, "tests")
      val jsonResp = JsonUtil.toJson(resp).getOrElse("""{"status":"error"}""")
      jsonResp must beEqualTo("""{"status":"ok","service-name":"tests","dependencies":["dep1","dep2","dep3"],"git-info":{"branch":"test-branch","branch.clean":"true","commit.sha":"1234","commit.date":"today"}}""")
    }
    def okWithMissing = apply {
      bp.get("git.commit.sha") returns Option.empty[String]
      bp.get("git.commit.date") returns Option.empty[String]

      val resp = StatusResponse.getStatusResponse(bp, "tests")
      val jsonResp = JsonUtil.toJson(resp).getOrElse("""{"status":"error"}""")
      jsonResp must beEqualTo("""{"status":"ok","service-name":"tests","dependencies":["dep1","dep2","dep3"],"git-info":{"branch":"test-branch","branch.clean":"true"}}""")
    }
  }



}
