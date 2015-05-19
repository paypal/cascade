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
package com.paypal.cascade.http.tests.actor

import akka.testkit.{TestProbe, TestActorRef}
import akka.actor.{Actor, ActorSystem}

/**
 * a container to hold a [[akka.testkit.TestActorRef]] and a [[akka.testkit.TestProbe]]. it automatically watches the ref and provides convenience functions for
 * sending messages from the probe, etc...
 * @param ref the [[akka.testkit.TestActorRef]]
 * @param probe the [[akka.testkit.TestProbe]] that's watching ref
 * @tparam T the type of actor inside the [[akka.testkit.TestActorRef]]
 */
class RefAndProbe[T <: Actor](val ref: TestActorRef[T], val probe: TestProbe) {

  probe.watch(ref)

  /**
   * send a message from the probe to the actor ref. after you call this, you'll
   * be able to call expectMsg, etc... on the probe
   * @param msg the message to send
   */
  def tell(msg: Any) {
    probe.send(ref, msg)
  }
}

object RefAndProbe {
  /**
   * convenience constructor on [[RefAndProbe]]
   */
  def apply[T <: Actor](ref: TestActorRef[T])(implicit actorSystem: ActorSystem) = {
    new RefAndProbe(ref, TestProbe())
  }
}
