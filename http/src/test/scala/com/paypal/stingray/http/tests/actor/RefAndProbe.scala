package com.paypal.stingray.http.tests.actor

import akka.testkit.{TestProbe, TestActorRef}
import akka.actor.{Actor, ActorSystem}

/**
 * a container to hold a [[TestActorRef]] and a [[TestProbe]]. it automatically watches the ref and provides convenience functions for
 * sending messages from the probe, etc...
 * @param ref the [[TestActorRef]]
 * @param probe the [[TestProbe]] that's watching ref
 * @tparam T the type of actor inside the [[TestActorRef]]
 */
class RefAndProbe[T <: Actor](val ref: TestActorRef[T], val probe: TestProbe) {

  probe.watch(ref)

  def tell(msg: Any) {
    probe.send(ref, msg)
  }
}

object RefAndProbe {
  def apply[T <: Actor](ref: TestActorRef[T])(implicit actorSystem: ActorSystem) = {
    new RefAndProbe(ref, TestProbe())
  }
}