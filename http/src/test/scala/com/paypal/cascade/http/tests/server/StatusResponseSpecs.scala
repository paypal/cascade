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
package com.paypal.cascade.http.tests.server

import org.specs2._
import org.specs2.mock.Mockito

import com.paypal.cascade.http.server.StatusResponse
import com.paypal.cascade.common.properties.BuildProperties
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.json.JsonUtil

/**
 * Tests for [[com.paypal.cascade.http.server.StatusResponse]].
 */
class StatusResponseSpecs extends Specification with Mockito { override def is = s2"""

  getStatusResponse
    properly creates json response                      ${Response().ok}
    does not fail when some keys are missing            ${Response().okWithMissing}
  """

  trait Context extends CommonImmutableSpecificationContext {
    val bp = mock[BuildProperties]
    bp.get("service.buildVersion") returns Option("1.2.3")
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
      jsonResp must beEqualTo("""{"status":"ok","service-name":"tests","build-version":"1.2.3","dependencies":["dep1","dep2","dep3"],"git-info":{"branch":"test-branch","branch-is-clean":"true","commit-sha":"1234","commit-date":"today"}}""")
    }
    def okWithMissing = apply {
      bp.get("git.commit.sha") returns Option.empty[String]
      bp.get("git.commit.date") returns Option.empty[String]

      val resp = StatusResponse.getStatusResponse(bp, "tests")
      val jsonResp = JsonUtil.toJson(resp).getOrElse("""{"status":"error"}""")
      jsonResp must beEqualTo("""{"status":"ok","service-name":"tests","build-version":"1.2.3","dependencies":["dep1","dep2","dep3"],"git-info":{"branch":"test-branch","branch-is-clean":"true"}}""")
    }
  }

}
