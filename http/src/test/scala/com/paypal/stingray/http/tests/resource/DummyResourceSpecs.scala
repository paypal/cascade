package com.paypal.stingray.http.tests.resource

import org.specs2.Specification
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import org.specs2.mock.Mockito
import spray.http._
import spray.http.HttpEntity._
import HttpHeaders._
import com.paypal.stingray.http.tests.matchers.SprayMatchers
import akka.actor.ActorSystem
import scala.util.Try

/**
 * Tests that exercise the [[com.paypal.stingray.http.resource.AbstractResourceActor]] abstract class,
 * via the [[com.paypal.stingray.http.tests.resource.DummyResource]] implementation.
 */
class DummyResourceSpecs extends Specification with Mockito { override def is = s2"""

  Tests that exercise the Resource abstract class, via the DummyResource implementation

  Sending a GetRequest
    should return pong                                                    ${Test().ping}
    should return pong after a request rewrite                            ${Test().pingRewrite}
    should have content language set                                      ${Test().language}

  Sending a language override via LanguageRequest
    should not override the language if set in resource                   ${Test().languageSetInResource}

  Sending a PostRequest =>
    should return the correct result and location header                  ${Test().pingPost}
    should return failures set in the resource                            ${Test().pingPostFail}


  """

  import DummyResource._

  trait Context extends CommonImmutableSpecificationContext with SprayMatchers {

    val resource = new DummyResource(_)

    implicit val actorSystem = ActorSystem("dummy-resource-specs")
  }

  case class Test() extends Context {

    def ping = {
      pingTest("ping")
    }

    def pingRewrite = {
      pingTest("ping-rewrite")
    }

    private def pingTest(path: String) = {
      val request = HttpRequest(uri = s"/$path?foo=bar").withHeaders(List(Accept(MediaTypes.`text/plain`)))
      resource must resultInCodeAndBodyLike(request,
        request => Try (GetRequest("bar")), StatusCodes.OK) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("\"pong\"")
        case Empty => true must beFalse
      }
    }

    def language = {
      val request = HttpRequest(uri = "/ping?foo=bar")
      resource must resultInResponseWithHeaderContaining(request,
        request => Try (GetRequest("bar")),
        RawHeader("Content-Language", "en-US"))
    }

    def languageSetInResource = {
      val request = HttpRequest(uri = "/moo")
      resource must resultInResponseWithHeaderContaining(request, request => Try (LanguageRequest()),
        RawHeader("Content-Language", "de"))
    }

    def pingPost = {
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://foo.com/ping").withEntity(HttpEntity(ContentTypes.`application/json`, """{"foo": "bar"}"""))
      (resource must resultInCodeAndBodyLike(request,
        request => Try (PostRequest("bar")),
        StatusCodes.Created) {
        case body @ NonEmpty(_, _) => body.asString must beEqualTo("\"pong\"")
        case Empty => true must beFalse
      }) and (resource must resultInResponseWithHeaderContaining(request,
        request => Try (PostRequest("bar")),
        HttpHeaders.Location("http://foo.com/ping/foobar")))
    }

    def pingPostFail = {
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://foo.com/ping").withEntity(HttpEntity(ContentTypes.`application/json`, """{"foo": "bar"}"""))
      resource must resultInCodeGivenData(request,
        request => Try (PostRequest("incorrect")),
        StatusCodes.BadRequest)
    }
  }

}
