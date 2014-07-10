package com.paypal.stingray.akka.tests.mailbox

import java.util.concurrent.TimeUnit

import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

import akka.actor._
import akka.dispatch.Envelope
import akka.testkit.{TestActorRef, TestKit}
import org.specs2.SpecificationLike

import com.paypal.stingray.akka.actor.UnhandledMessageException
import com.paypal.stingray.akka.mailbox.ExpiringBoundedMailbox
import com.paypal.stingray.akka.tests.actor.ActorSpecification
import com.paypal.stingray.common.tests.future._
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[ExpiringBoundedMailbox]]
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
    val mailbox = ExpiringBoundedMailbox(1, FiniteDuration(10, TimeUnit.MILLISECONDS), FiniteDuration(10, TimeUnit.MILLISECONDS))
      .create(Some(testActor), Some(system))
  }

  case class Normal() extends Context {
    def ok = apply {
      mailbox.enqueue(testActor, Envelope(Ping(), testActor, system))
      mailbox.dequeue() must beEqualTo(Envelope(Ping(), testActor, system))
    }
  }

  case class Failing() extends Context {
    def full = apply {
      mailbox.enqueue(testActor, Envelope(Ping(), sendActor, system)) //1 in the mailbox
      mailbox.enqueue(testActor, Envelope(Ping(), sendActor, system)) //this should fail
      promise.future.toTry must beAFailedTry.withThrowable[UnhandledMessageException]
    }

    def stale = apply {
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
