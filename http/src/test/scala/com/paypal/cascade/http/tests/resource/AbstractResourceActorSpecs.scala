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
package com.paypal.cascade.http.tests.resource

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import akka.actor._
import akka.testkit.{TestKit, TestProbe}
import spray.http.StatusCodes._
import spray.http._
import spray.routing.RequestContext
import com.fasterxml.jackson.databind.JsonMappingException
import org.specs2.SpecificationLike
import org.specs2.execute.Result

import com.paypal.cascade.akka.tests.actor.ActorSpecification
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.http.resource.HttpResourceActor._
import com.paypal.cascade.http.resource._
import com.paypal.cascade.http.util.HttpUtil

/**
 * Tests that exercise the [[com.paypal.cascade.http.resource.AbstractResourceActor]] abstract class
 */
class AbstractResourceActorSpecs
  extends TestKit(ActorSystem("abstract-resource-specs"))
  with SpecificationLike
  with ActorSpecification { override def is = s2"""

  When sending an error, AbstractResourceActor should
    forward the error to createErrorResponse with sendError                  ${test().err}
    send the specified error code with an empty body with sendErrorCode      ${test().errCode}
    send the specified error code and response body with sendErrorResponse   ${test().errResponse}

  Sending a request object should
    send back a response                                                     ${test().response}
    send back a serialized JSON response                                     ${test().jsonResponse}
    send an error if response object can't be serialized                     ${test().badJsonResponse}

  Overriding resourceReceive should
    allow for users to match on Status.Failure                               ${test().overriddenStatusFailure}
    fall back to createErrorResponse otherwise                               ${test().fallbackStatusFailure}

  """

  trait Context extends CommonImmutableSpecificationContext {

    class TestResource(ctx: ResourceContext) extends AbstractResourceActor(ctx) {

      /**
       * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
       * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
       * safe to store mutable state during this receive function.
       * When the request is finished, complete must be called.
       * @return The receive function to be applied when a parsed request object or other actor message is received
       */
      override protected def resourceReceive: PartialFunction[Any, Unit] = {
        case ProcessRequest("") => complete(HttpResponse(OK, "pong"))
        case ProcessRequest("json") => completeToJSON(OK, PongResponse("pong"))
        case ProcessRequest("badjson") => completeToJSON(OK, SourObject) //trying to serialize something we shouldn't
        case ProcessRequest("error") => sendError(CascadeUserException)
        case ProcessRequest("errorCode") => sendErrorCodeResponse(InternalServerError)
        case ProcessRequest("errorResponse") => sendErrorResponse(InternalServerError, ErrorResponse("oops"))
        case Status.Failure(CustomStatusFailureException) => sendErrorResponse(InternalServerError, ErrorResponse("got custom exception"))
      }

      case class PongResponse(resp: String)
      case class ErrorResponse(error: String)

      override val responseContentType: ContentType = ContentTypes.`text/plain(UTF-8)`
      override val responseLanguage: Option[Language] = None
    }

    //TODO: make a Scalacheck generator
    def fakeContext(ref: ActorRef, parsedString: String = ""): ResourceContext = {
      ResourceContext(RequestContext(HttpRequest(), ref, Uri.Path("")),
                      _ => scala.util.Success(parsedString),
                      Option(ref), Duration(2, TimeUnit.SECONDS))
    }

    case object CascadeUserException extends Exception("an exception defined by a cascade user")
    case object GenericException extends Exception("generic downstream exception")
    case object CustomStatusFailureException extends Exception("this is a custom exception that is matched in a Status.Failure")
    object SourObject {
      lazy val thisWillThrow = {
        throw new RuntimeException("accessor throws an exception, and Jackson will respond with a failure")
      }
    }
  }

  case class test() extends Context {

    private def probeAndTest[T](parsedMessage: String, expectedResponse: HttpResponse): Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(fakeContext(probe.ref, parsedMessage))))
      resourceRef ! Start
      try {
        probe.receiveOne(Duration(2, TimeUnit.SECONDS)) must beEqualTo (expectedResponse)
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }

    private def probeAndTestStatusFailure[T](t: Throwable, expectedResponse: HttpResponse): Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(fakeContext(probe.ref, "probeAndTestStatusFailure"))))
      resourceRef ! Status.Failure(t)
      try {
        probe.receiveOne(Duration(2, TimeUnit.SECONDS)) must beEqualTo (expectedResponse)
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }

    def response: Result = {
      probeAndTest("", HttpResponse(OK, "pong"))
    }

    def jsonResponse: Result = {
      probeAndTest("json", HttpResponse(OK, """{"resp":"pong"}"""))
    }

    def badJsonResponse: Result = {
      probeAndTest("badjson", HttpResponse(InternalServerError,  HttpEntity(ContentTypes.`application/json`, s""""Could not write response to json: ${classOf[JsonMappingException].getSimpleName}"""")))
    }

    def err: Result = {
      probeAndTest("error", HttpResponse(InternalServerError, HttpUtil.toJsonBody(s"Error in request execution: ${CascadeUserException.getClass.getSimpleName}")))
    }

    def errCode: Result = {
      probeAndTest("errorCode", HttpResponse(InternalServerError))
    }

    def errResponse: Result = {
      probeAndTest("errorResponse", HttpResponse(InternalServerError, HttpEntity(ContentTypes.`application/json`, """{"error":"oops"}""")))
    }

    def overriddenStatusFailure: Result = {
      probeAndTestStatusFailure(CustomStatusFailureException, HttpResponse(InternalServerError, HttpEntity(ContentTypes.`application/json`, """{"error":"got custom exception"}""")))
    }

    def fallbackStatusFailure: Result = {
      probeAndTestStatusFailure(GenericException, HttpResponse(InternalServerError, HttpUtil.toJsonBody(s"Error in request execution: ${GenericException.getClass.getSimpleName}")))
    }

  }

}
