package com.paypal.stingray.http.tests.matchers

import akka.actor.Actor
import org.specs2.matcher.{MatchResult, Expectable, Matcher}
import com.paypal.stingray.http.tests.actor.RefAndProbe
import scala.util.Try

trait RefAndProbeMatchers {

  class RefAndProbeIsStopped[T <: Actor]() extends Matcher[RefAndProbe[T]] {
    override def apply[S <: RefAndProbe[T]](r: Expectable[S]): MatchResult[S] = {
      val refAndProbe = r.value
      val res = Try(refAndProbe.probe.expectTerminated(refAndProbe.ref))
      result(res.isSuccess, s"${refAndProbe.ref} has stopped", s"${refAndProbe.ref} is not stopped", r)
    }
  }

  def beStopped[T <: Actor] = {
    new RefAndProbeIsStopped[T]
  }

}
