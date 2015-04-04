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
package com.paypal.cascade.akka.tests.actor

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
   * A stubbed actor generator, yielding actors that extend [[akka.testkit.TestProbe]] with configurable message logic
   * @param expect a partial function that specifies logic for incoming messages
   * @tparam T the response message type
   * @return a TestProbe with `expect` logic embedded, ready for testing
   */
  protected def mockActorWithProbe[T](expect: PartialFunction[Any, T]): TestProbe = {
    val probe = TestProbe()
    probe.setAutoPilot(new TestActor.AutoPilot {

      def invalidMessage(msg: Any): T = throw new IllegalStateException(s"Invalid message sent $msg")

      override def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
        implicit val ec = actorSystem.dispatchers.lookup(CallingThreadDispatcher.Id)

        Try { expect.applyOrElse(msg, invalidMessage) } match {
          case TrySuccess(response) => Future.successful(response) pipeTo sender
          case TryFailure(error) => Future.failed(error) pipeTo sender
        }
        TestActor.KeepRunning
      }
    })
    probe
  }

  /**
   * A wrapper to [[mockActorWithProbe]] that yields the probe's ActorRef instead
   * @param expect a partial function that specifies logic for incoming messages
   * @tparam T the response message type
   * @return a TestProbe ActorRef with `expect` logic embedded, ready for testing
   */
  protected def mockActor[T](expect: PartialFunction[Any, T]): ActorRef = mockActorWithProbe(expect).ref

}
