package com.paypal.stingray.common.tests.actor

import org.specs2._
import akka.pattern.ask
import com.paypal.stingray.common.actor._
import akka.actor._
import akka.testkit._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext
import akka.util.Timeout
import java.util.concurrent.TimeUnit

/**
 * Tests [[com.paypal.stingray.common.actor.CommonActor]]
 */
class CommonActorSpecs
  extends TestKit(ActorSystem("CommonActorSpecs"))
  with SpecificationLike
  with ActorSpecification
  with ScalaCheck { def is = s2"""

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

    def failureCase = this {
      (testActor ? "hello").mapTo[String].toTry must beFailedTry[String].withThrowable[UnhandledMessageException]
    }

  }

}
