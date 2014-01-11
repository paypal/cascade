package com.paypal.stingray.http.tests.resource

import org.specs2.Specification
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import org.specs2.mock.Mockito
import spray.http._
import spray.http.HttpEntity._
import HttpHeaders._
import com.paypal.stingray.http.tests.matchers.SprayMatchers

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/12/13
 * Time: 1:14 PM
 */
class DummyResourceSpecs extends Specification with Mockito { override def is =

  "DummyResourceSpecs".title                                              ^
                                                                          p^
    "GET /ping  =>"                                                       ^
      "should return pong"                                                ! Test().ping ^
      "should have the right headers set if unauthorized"                 ! Test().unauthorized ^
                                                                          p^
    "POST /ping  =>"                                                      ^
      "should return pong"                                                ! Test().pingPost ^
                                                                          p^
    "PUT /ping  =>"                                                       ^
      "should return pong"                                                ! Test().pingPut ^
                                                                          end

  case class Test() extends context {
    def ping = {
      val request = HttpRequest(uri = "/ping?foo=bar").withHeaders(List(Accept(MediaTypes.`text/plain`)))
      resource must resultInCodeAndBodyLike(request, Map(), StatusCodes.OK) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("pong")
        case Empty => true must beFalse
      }
    }

    def unauthorized = {
      val request = HttpRequest(uri = "/ping").withHeaders(List(Accept(MediaTypes.`text/plain`), RawHeader("unauthorized", "true")))
      (resource must resultInCodeGivenData(request, Map(), StatusCodes.Unauthorized)) and
      (resource must resultInResponseWithHeaderContaining(request, Map(), `WWW-Authenticate`(HttpChallenge("OAuth", request.uri.authority.host.toString))))
    }

    def pingPost = {
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://foo.com/ping").withEntity(HttpEntity(ContentTypes.`application/json`, """{"foo": "bar"}"""))
      (resource must resultInCodeAndBodyLike(request, Map(), StatusCodes.Created) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("pong")
        case Empty => true must beFalse
      }) and (resource must resultInResponseWithHeaderContaining(request, Map(), HttpHeaders.Location("http://foo.com/ping/foobar")))
    }

    def pingPut = {
      val request = HttpRequest(method = HttpMethods.PUT, uri = "/ping").withHeaders(List(Accept(MediaTypes.`text/plain`)))
      resource must resultInCodeAndBodyLike(request, Map(), StatusCodes.OK) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("pong")
        case Empty => true must beFalse
      }
    }
  }

  trait context extends CommonImmutableSpecificationContext with SprayMatchers {

    val resource = new DummyResource
  }
}
