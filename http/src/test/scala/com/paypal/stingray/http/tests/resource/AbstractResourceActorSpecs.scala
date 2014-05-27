package com.paypal.stingray.http.tests.resource

import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import spray.http._
import com.paypal.stingray.http.resource._
import spray.http.StatusCodes._
import akka.actor._
import com.paypal.stingray.http.resource.ResourceHttpActor.{RequestIsProcessed, CheckSupportedFormats, SupportedFormats}
import spray.http.HttpResponse
import akka.testkit.{TestProbe, TestKit}
import org.specs2.SpecificationLike
import akka.actor.Status.Failure
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.paypal.stingray.akka.tests.actor.ActorSpecification

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

    class TestResource(ref: ActorRef) extends AbstractResourceActor(ref) {

      /**
       * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
       * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
       * safe to store mutable state during this receive function.
       * When the request is finished, [[complete]] must be called
       * @return The receive function to be applied when a parsed request object or other actor message is received
       */
      override protected def processRequest: PartialFunction[Any, Unit] = {
        case Unit => complete(HttpResponse(OK, "pong"))
      }
    }
  }

  case class test() extends Context {
    def formats = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(probe.ref)))
      resourceRef ! CheckSupportedFormats
      val expected: SupportedFormats = SupportedFormats(List(ContentTypes.`application/json`),
        ContentTypes.`application/json`,
        Option(Language("en", "US")))
      probe.receiveOne(Duration(250, TimeUnit.MILLISECONDS)) must beEqualTo (expected)
    }

    def response = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(probe.ref)))
      resourceRef ! Unit
      probe.receiveOne(Duration(250, TimeUnit.MILLISECONDS)) must beEqualTo (RequestIsProcessed(HttpResponse(OK, "pong"), None))
    }

    case object GenericException extends Exception("generic downstream exception")

    def err = {
      val probe = TestProbe()
      val resourceRef = system.actorOf(Props(new TestResource(probe.ref)))
      val expected: Failure = Status.Failure(GenericException)
      resourceRef ! expected
      probe.receiveOne(Duration(250, TimeUnit.MILLISECONDS)) must beEqualTo (expected)
    }
  }

}
