package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import spray.http._
import com.paypal.stingray.http.resource._
import spray.http.StatusCodes._
import akka.actor._
import com.paypal.stingray.http.resource.HttpResourceActor._
import akka.testkit.{TestProbe, TestKit}
import org.specs2.SpecificationLike
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.paypal.stingray.akka.tests.actor.ActorSpecification
import com.paypal.stingray.http.resource.HttpResourceActor.ProcessRequest
import spray.http.HttpResponse
import spray.http.Language
import com.paypal.stingray.http.resource.HttpResourceActor.RequestIsProcessed
import akka.actor.Status.Failure
import com.paypal.stingray.http.resource.HttpResourceActor.SupportedFormats
import org.specs2.matcher.MatchResult
import org.specs2.execute.Result
import spray.routing.RequestContext
import com.paypal.stingray.http.util.HttpUtil
import com.paypal.stingray.common.constants.ValueConstants._
import spray.http.HttpRequest
import com.paypal.stingray.http.resource.HttpResourceActor.ProcessRequest
import spray.routing.RequestContext
import spray.http.Language
import com.paypal.stingray.http.resource.HttpResourceActor.ResourceContext
import scala.Some
import spray.http.HttpResponse

/**
 * Tests that exercise the [[com.paypal.stingray.http.resource.AbstractResourceActor]] abstract class
 */
class AbstractResourceActorSpecs
  extends TestKit(ActorSystem("abstract-resource-specs"))
  with SpecificationLike
  with ActorSpecification { override def is = s2"""

  When receiving an error, AbstractResourceActor should
    forward that error                                           ${test().err}

  Sending a request object should
    send back a response                                         ${test().response}

  """

  trait Context extends CommonImmutableSpecificationContext {

    class TestResource(ctx: ResourceContext) extends AbstractResourceActor(ctx) {

      /**
       * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
       * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
       * safe to store mutable state during this receive function.
       * When the request is finished, [[complete]] must be called
       * @return The receive function to be applied when a parsed request object or other actor message is received
       */
      override protected def resourceReceive: PartialFunction[Any, Unit] = {
        case ProcessRequest(Unit) => complete(HttpResponse(OK, "pong"))
      }

      override val responseContentType: ContentType = ContentTypes.`text/plain(UTF-8)`
      override val responseLanguage: Option[Language] = None
    }

    //TODO: make a Scalacheck generator
    def fakeContext(ref: ActorRef): ResourceContext = {
      ResourceContext(RequestContext(HttpRequest(), ref, Uri.Path("")),
                      _ => scala.util.Success(Unit),
                      Some(ref), Duration(2, TimeUnit.SECONDS))
    }
  }

  case class test() extends Context {

    def response: Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(fakeContext(probe.ref))))
      resourceRef ! Start
      try {
        probe.receiveOne(Duration(1, TimeUnit.SECONDS)) must beEqualTo (HttpResponse(OK, "pong"))
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }

    def err: Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(fakeContext(probe.ref))))
      case object GenericException extends Exception("generic downstream exception")
      resourceRef ! Status.Failure(GenericException)
      try {
        probe.receiveOne(Duration(2, TimeUnit.SECONDS)) must beEqualTo(
          HttpResponse(InternalServerError, HttpUtil.coerceError(GenericException.getMessage.getBytes(charsetUtf8))))
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }
  }

}
