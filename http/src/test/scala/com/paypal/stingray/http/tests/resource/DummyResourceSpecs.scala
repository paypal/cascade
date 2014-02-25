package com.paypal.stingray.http.tests.resource

import org.specs2.Specification
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import org.specs2.mock.Mockito
import spray.http._
import spray.http.HttpEntity._
import HttpHeaders._
import com.paypal.stingray.http.tests.matchers.SprayMatchers

/**
 * Tests that exercise the [[com.paypal.stingray.http.resource.AbstractResource]] abstract class,
 * via the [[com.paypal.stingray.http.tests.resource.DummyResource]] implementation.
 */
class DummyResourceSpecs extends Specification with Mockito { override def is = s2"""

  Tests that exercise the Resource abstract class, via the DummyResource implementation

  GET /ping =>
    should return pong                                                    ${Test().ping}
    should have the right headers set if unauthorized                     ${Test().unauthorized}

  POST /ping =>
    should return pong                                                    ${Test().pingPost}

  PUT /ping =>
    should return pong                                                    ${Test().pingPut}
  """

  case class Test() extends context {
    def ping = {
      val request = HttpRequest(uri = "/ping?foo=bar").withHeaders(List(Accept(MediaTypes.`text/plain`)))
      resource must resultInCodeAndBodyLike(request, resource.doGet, Map(), StatusCodes.OK) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("pong")
        case Empty => true must beFalse
      }
    }

    def unauthorized = {
      val request = HttpRequest(uri = "/ping").withHeaders(List(Accept(MediaTypes.`text/plain`), RawHeader("unauthorized", "true")))
      (resource must resultInCodeGivenData(request, resource.doGet, Map(), StatusCodes.Unauthorized)) and
      (resource must resultInResponseWithHeaderContaining(request, resource.doGet, Map(), `WWW-Authenticate`(HttpChallenge("OAuth", request.uri.authority.host.toString))))
    }

    def pingPost = {
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://foo.com/ping").withEntity(HttpEntity(ContentTypes.`application/json`, """{"foo": "bar"}"""))
      (resource must resultInCodeAndBodyLike(request, resource.doPostAsCreate, Map(), StatusCodes.Created) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("pong")
        case Empty => true must beFalse
      }) and (resource must resultInResponseWithHeaderContaining(request, resource.doPostAsCreate, Map(), HttpHeaders.Location("http://foo.com/ping/foobar")))
    }

    def pingPut = {
      val request = HttpRequest(method = HttpMethods.PUT, uri = "/ping").withHeaders(List(Accept(MediaTypes.`text/plain`)))
      resource must resultInCodeAndBodyLike(request, resource.doPut, Map(), StatusCodes.OK) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("pong")
        case Empty => true must beFalse
      }
    }
  }

  trait context extends CommonImmutableSpecificationContext with SprayMatchers {

    val resource = new DummyResource
  }
}
