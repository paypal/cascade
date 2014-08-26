/**
 * Copyright 2013-2014 PayPal
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
package com.paypal.cascade.akka.tests.actor

import org.specs2._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import akka.pattern.ask
import akka.actor._
import akka.testkit._
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import com.paypal.cascade.akka.actor.{UnhandledMessageException, ServiceActor}
import com.paypal.cascade.common.tests.future._
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests [[com.paypal.cascade.akka.actor.CommonActor]]
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
