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
package com.paypal.cascade.akka.tests.mailbox

import java.util.concurrent.TimeUnit

import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

import akka.actor._
import akka.dispatch.Envelope
import akka.testkit.{TestActorRef, TestKit}
import org.specs2.SpecificationLike

import com.paypal.cascade.akka.actor.UnhandledMessageException
import com.paypal.cascade.akka.mailbox.ExpiringBoundedMailbox
import com.paypal.cascade.akka.tests.actor.ActorSpecification
import com.paypal.cascade.common.tests.future._
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.cascade.akka.mailbox.ExpiringBoundedMailbox]]
 */
class ExpiringBoundedMailboxSpecs
  extends TestKit(ActorSystem("ExpiringBoundedMailbox"))
  with SpecificationLike
  with ActorSpecification { override def is = s2"""

    ImmediatelyFailingBoundedMailbox is a Bounded akka mailbox that will not deliver messages if they
    take too long to enqueue or if they are expired by the time they are dequeued.

    Sending a message to an actor using an ImmediatelyFailingBoundedMailbox should
      succeed if the mailbox is not at capacity                               ${Normal().ok}
      fail if the mailbox is full and not being drained                       ${Failing().full}
      fail if the message is stale by the time the message is dequeued        ${Failing().stale}

    """

  trait Context extends CommonImmutableSpecificationContext {

    lazy val promise = Promise[Pong]()
    lazy val testActor = TestActorRef[TestActor](Props[TestActor].withMailbox("test-mailbox"))
    lazy val sendActor = TestActorRef[TestPromiseActor](new TestPromiseActor(promise)) //normal mailbox

    //to test a mailbox instance directly
    def mailboxWithExprMillis(expr: Int) =
      ExpiringBoundedMailbox(1, FiniteDuration(10, TimeUnit.MILLISECONDS), FiniteDuration(expr, TimeUnit.MILLISECONDS))
        .create(Some(testActor), Some(system))
  }

  case class Normal() extends Context {
    def ok = apply {
      val mailbox = mailboxWithExprMillis(2000)
      mailbox.enqueue(testActor, Envelope(Ping(), testActor, system))
      mailbox.dequeue() must beEqualTo(Envelope(Ping(), testActor, system))
    }
  }

  case class Failing() extends Context {
    def full = apply {
      val mailbox = mailboxWithExprMillis(2000)
      mailbox.enqueue(testActor, Envelope(Ping(), sendActor, system)) //1 in the mailbox
      mailbox.enqueue(testActor, Envelope(Ping(), sendActor, system)) //this should fail
      promise.future.toTry must beAFailedTry.withThrowable[UnhandledMessageException]
    }

    def stale = apply {
      val mailbox = mailboxWithExprMillis(10)
      mailbox.enqueue(testActor, Envelope(Ping(), sendActor, system)) //1 in the mailbox
      Thread.sleep(100) //let the message go stale
      mailbox.dequeue()
      promise.future.toTry must beAFailedTry.withThrowable[UnhandledMessageException]
    }
  }

}

class TestActor extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case Ping()  => sender ! Pong()
    case Status.Failure(e) => log.debug(s"failed msg: ${e.getMessage}")
    case m => log.debug("unexpected msg " + m)
  }
}

class TestPromiseActor(p: Promise[Pong]) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case Ping()  => sender ! Pong(); p.complete(Success(Pong()))
    case Status.Failure(e) => log.debug(s"failed msg: ${e.getMessage}"); p.complete(Failure(e))
  }
}

case class Ping()
case class Pong()
