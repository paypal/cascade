package com.paypal.stingray.akka.tests.actor

import org.specs2._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import akka.pattern.ask
import akka.actor._
import akka.testkit._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import com.paypal.stingray.akka.actor.{UnhandledMessageException, ServiceActor}

/**
 * Tests [[CommonActor]]
 */
class CommonActorSpecs
  extends TestKit(ActorSystem("CommonActorSpecs"))
  with SpecificationLike
  with ScalaCheck
  with ActorSpecification { override def is = s2"""

  Passing an unhandled exception results in an UnhandledMessageException ${Message().failureCase}

  """

  trait Context
    extends CommonImmutableSpecificationContext {

    class TestActor extends ServiceActor {
      def receive = {
        case num: Int =>
      }
    }

    lazy val testActor: ActorRef = TestActorRef(new TestActor)
    implicit lazy val timeout = Timeout(10, TimeUnit.SECONDS)
  }

  case class Message() extends Context {

    def failureCase = forAll(arbitrary[String]) { str =>
      (testActor ? str).mapTo[String].toTry must beFailedTry[String].withThrowable[UnhandledMessageException]
    }

  }

}
