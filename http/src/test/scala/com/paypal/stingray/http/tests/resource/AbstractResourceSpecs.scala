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
import akka.actor.{Status, Actor, ActorSystem}
import com.paypal.stingray.http.resource.ResourceHttpActor.{RequestIsProcessed, SupportedFormats}

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

    class TestActor extends Actor {
      var supportedFormatsReceived = false
      var errorReceived = false
      var resultReceived = false

      override def receive: Actor.Receive = {
        case _: SupportedFormats => supportedFormatsReceived = true
        case _: Status.Failure => errorReceived = true
        case _: RequestIsProcessed => resultReceived = true
      }
    }


    val testResource = new TestResource

    //
    class TestResource extends AbstractResourceActor(null) {
      override def isAuthorized(r: HttpRequest): Future[Option[Unit]] = {
        if (r.headers.find(_.lowercaseName == "unauthorized").isEmpty) {
          Some().continue
        } else {
          halt(StatusCodes.Unauthorized)
        }
      }

      /**
       * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
       * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
       * safe to store mutable state during this receive function.
       * When the request is finished, [[complete]] must be called
       * @return The receive function to be applied when a parsed request object or other actor message is received
       */
      override protected def processRequest: PartialFunction[Any, Unit] = {
        case Unit => sender ! Unit
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
