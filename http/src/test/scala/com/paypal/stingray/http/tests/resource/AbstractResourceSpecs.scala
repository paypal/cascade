package com.paypal.stingray.http.tests.resource

import org.specs2.Specification
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import org.specs2.mock.Mockito
import spray.http._
import com.paypal.stingray.http.tests.matchers.SprayMatchers
import com.paypal.stingray.http.resource._
import spray.http.StatusCodes._
import scala.concurrent.Future
import spray.http.HttpRequest
import scala.Some
import scala.util.Try
import akka.actor.ActorSystem

/**
 * Tests that exercise the [[com.paypal.stingray.http.resource.AbstractResourceActor]] abstract class
 */
class AbstractResourceSpecs extends Specification with Mockito { override def is = s2"""

  acceptableContentTypes
    returns the default application/json                          ${CTypes().acceptable}

  responseContentType
    returns the default application/json                          ${CTypes().response}

  errorResponse
    properly returns a 500                                        ${ErrorResponse().returns500}

  jsonOKResponse
    serializes json and returns OK status                         ${JsonOK().returnsOK}

  toJsonBody
    Success returns proper http response                          ${JsonBody().ok}
    Failure returns error in json format                          ${JsonBody().error}

  """

  trait Context extends CommonImmutableSpecificationContext with SprayMatchers {
    implicit val actorSystem = ActorSystem("abstract-resource-specs")

    val fullResource = new DummyResource

    val testResource = new TestResource

    class TestResource extends AbstractResourceActor {
      override def isAuthorized(r: HttpRequest): Future[Option[Unit]] = {
        if (r.headers.find(_.lowercaseName == "unauthorized").isEmpty) {
          Some().continue
        } else {
          halt(StatusCodes.Unauthorized)
        }
      }
    }
  }

  case class CTypes() extends Context {
    def acceptable = {
      testResource.acceptableContentTypes must beEqualTo(List(ContentTypes.`application/json`))
    }
    def response = {
      testResource.responseContentType must beEqualTo(ContentTypes.`application/json`)
    }
  }

  case class ErrorResponse() extends Context {
    def returns500 = {
      val error = testResource.errorResponse(new Exception("Some Error"))
      (error must beAnInstanceOf[HttpResponse]) and (error.status must beEqualTo(InternalServerError))
    }
  }

  case class JsonOK() extends Context {
    def returnsOK = {
      val body = """{"key":"value""""
      val resp = testResource.jsonOKResponse(body)
      (resp must beAnInstanceOf[HttpResponse]) and (resp.status must beEqualTo(OK))
    }
  }

  case class JsonBody() extends Context {
    def ok = {
      val body = Map("key" -> "value")
      val expected = """{"key":"value"}"""
      val resp = testResource.toJsonBody(body)
      (resp must beAnInstanceOf[HttpEntity]) and (resp.data.asString must beEqualTo(expected))
    }
    def error = {
      class Foo
      val body = new Foo
      val resp = testResource.toJsonBody(body)
      (resp must beAnInstanceOf[HttpEntity]) and (resp.data.asString must contain("errors"))
    }
  }

}
