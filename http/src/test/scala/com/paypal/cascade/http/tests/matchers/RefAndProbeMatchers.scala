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
package com.paypal.cascade.http.tests.matchers

import akka.actor.Actor
import org.specs2.matcher.{MatchResult, Expectable, Matcher}
import com.paypal.cascade.http.tests.actor.RefAndProbe
import scala.util.Try

/**
 * this trait has specs2 matcher functionality for the [[com.paypal.cascade.http.tests.actor.RefAndProbe]] class. Mix it into your [[org.specs2.Specification]] (or trait/class inside your specification).
 *
 * Example usage:
 *
 * {{{
 *  class MySpec extends Specification { override def is = s2"""
 *    MySpec1 ${spec1}
 *    """
 *
 *    implicit system = ActorSystem("hello-world")
 *
 *    def mySpec1 = {
 *      val r = RefAndProbe(TestActorRef(new MyActor))
 *      //...
 *      system.stop(r.ref)
 *      r must beStopped
 *    }
 *  }
 * }}}
 *
 */
trait RefAndProbeMatchers {

  /**
   * the matcher for testing whether the [[akka.testkit.TestActorRef]] inside the [[com.paypal.cascade.http.tests.actor.RefAndProbe]] is stopped
   * @tparam T the [[akka.actor.Actor]] that the [[akka.testkit.TestActorRef]] contains
   */
  class RefAndProbeIsStopped[T <: Actor]() extends Matcher[RefAndProbe[T]] {
    override def apply[S <: RefAndProbe[T]](r: Expectable[S]): MatchResult[S] = {
      val refAndProbe = r.value
      val res = Try(refAndProbe.probe.expectTerminated(refAndProbe.ref))
      result(res.isSuccess, s"${refAndProbe.ref} has stopped", s"${refAndProbe.ref} is not stopped", r)
    }
  }

  /**
   * the matcher function to test whether the [[akka.testkit.TestActorRef]] inside a [[com.paypal.cascade.http.tests.actor.RefAndProbe]] is stopped
   *
   * Example usage:
   *
   * {{{
   *   val refAndProbe = RefAndProbe(TestActorRef(new MyActor))
   *   //do stuff with refAndProbe.ref
   *   ...
   *   //shut down refAndProbe.ref
   *   refAndProbe must beStopped
   * }}}
   * @tparam T the [[akka.actor.Actor]] that the [[akka.testkit.TestActorRef]] contains
   * @return the new matcher.
   */
  def beStopped[T <: Actor] = {
    new RefAndProbeIsStopped[T]
  }

}
