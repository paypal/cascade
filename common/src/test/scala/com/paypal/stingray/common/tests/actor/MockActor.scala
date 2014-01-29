package com.paypal.stingray.common.tests.actor

import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.pipe
import akka.testkit.{CallingThreadDispatcher, TestActor, TestProbe}
import scala.concurrent.Future
import scala.util.{Try, Success => TrySuccess, Failure => TryFailure}

/**
 * A partially-specified Actor harness, for use with testing Akka Actors
 */
trait MockActor {

  /** The ActorSystem for Actors made here */
  implicit def actorSystem: ActorSystem

  /**
   * A stubbed actor generator, yielding actors that extend [[TestProbe]] with configurable message logic
   * @param expect a partial function that specifies logic for incoming messages
   * @tparam T the response message type
   * @return a TestProbe ActorRef with `expect` logic embedded, ready for testing
   */
  protected def mockActor[T](expect: PartialFunction[Any, T]): ActorRef = {
    val probe = TestProbe()
    probe.setAutoPilot(new TestActor.AutoPilot {

      def invalidMessage(msg: Any): T = throw new IllegalStateException(s"Invalid message sent $msg")

      override def run(sender: ActorRef,
                       msg: Any): TestActor.AutoPilot = {
        implicit val ec = actorSystem.dispatchers.lookup(CallingThreadDispatcher.Id)

        Try { expect.applyOrElse(msg, invalidMessage) } match {
          case TrySuccess(response) => Future.successful(response) pipeTo sender
          case TryFailure(error) => Future.failed(error) pipeTo sender
        }
        TestActor.KeepRunning
      }
    })
    probe.ref
  }

}
