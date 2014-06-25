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

/**
 * Tests that exercise the [[com.paypal.stingray.http.resource.AbstractResourceActor]] abstract class
 */
class AbstractResourceActorSpecs
  extends TestKit(ActorSystem("abstract-resource-specs"))
  with SpecificationLike
  with ActorSpecification { override def is = s2"""

  requesting acceptable formats should
    return the correct defaults                                  ${test().formats}
  sending a request object should
    send back a response                                         ${test().response}
  When receiving an error, AbstractResourceActor should
    forward that error                                           ${test().err}


  """

  trait Context extends CommonImmutableSpecificationContext {

    class TestResource(ref: ResourceContext) extends AbstractResourceActor(ref) {

      /**
       * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
       * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
       * safe to store mutable state during this receive function.
       * When the request is finished, complete must be called.
       * @return The receive function to be applied when a parsed request object or other actor message is received
       */
      override protected def resourceReceive: PartialFunction[Any, Unit] = {
        case ProcessRequest(Unit) => complete(HttpResponse(OK, "pong"))
      }
    }
  }

  case class test() extends Context {
    def formats: Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(ResourceContext(probe.ref))))
      resourceRef ! CheckSupportedFormats
      val expected: SupportedFormats = SupportedFormats(List(ContentTypes.`application/json`),
        ContentTypes.`application/json`,
        Option(Language("en", "US")))
      try {
        probe.receiveOne(Duration(250, TimeUnit.MILLISECONDS)) must beEqualTo (expected)
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }

    def response: Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(ResourceContext(probe.ref))))
      resourceRef ! ProcessRequest(Unit)
      try {
        probe.receiveOne(Duration(250, TimeUnit.MILLISECONDS)) must beEqualTo (RequestIsProcessed(HttpResponse(OK, "pong"), None))
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }

    case object GenericException extends Exception("generic downstream exception")

    def err: Result = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(ResourceContext(probe.ref))))
      val expected: Failure = Status.Failure(GenericException)
      resourceRef ! expected
      try {
        probe.receiveOne(Duration(250, TimeUnit.MILLISECONDS)) must beEqualTo (expected)
      } catch {
        case t: Throwable => org.specs2.execute.Failure(t.getMessage)
      }
    }
  }

}
