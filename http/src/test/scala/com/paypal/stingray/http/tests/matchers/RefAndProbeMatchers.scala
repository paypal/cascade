package com.paypal.stingray.http.tests.matchers

import akka.actor.Actor
import org.specs2.matcher.{MatchResult, Expectable, Matcher}
import com.paypal.stingray.http.tests.actor.RefAndProbe
import scala.util.Try

/**
 * this trait has specs2 matcher functionality for the [[RefAndProbe]] class. Mix it into your [[org.specs2.Specification]] (or trait/class inside your specification).
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
   * the matcher for testing whether the [[akka.testkit.TestActorRef]] inside the [[RefAndProbe]] is stopped
   * @tparam T the [[Actor]] that the [[akka.testkit.TestActorRef]] contains
   */
  class RefAndProbeIsStopped[T <: Actor]() extends Matcher[RefAndProbe[T]] {
    override def apply[S <: RefAndProbe[T]](r: Expectable[S]): MatchResult[S] = {
      val refAndProbe = r.value
      val res = Try(refAndProbe.probe.expectTerminated(refAndProbe.ref))
      result(res.isSuccess, s"${refAndProbe.ref} has stopped", s"${refAndProbe.ref} is not stopped", r)
    }
  }

  /**
   * the matcher function to test whether the [[akka.testkit.TestActorRef]] inside a [[RefAndProbe]] is stopped
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
   * @tparam T the [[Actor]] that the [[akka.testkit.TestActorRef]] contains
   * @return the new matcher.
   */
  def beStopped[T <: Actor] = {
    new RefAndProbeIsStopped[T]
  }

}
