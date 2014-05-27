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

}
