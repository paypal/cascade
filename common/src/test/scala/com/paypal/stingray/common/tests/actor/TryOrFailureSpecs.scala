package com.paypal.stingray.common.tests.actor

import org.specs2._
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._
import scala.util.Try
import com.paypal.stingray.common.actor._
import akka.actor._

/**
 * Tests for implicit [[com.paypal.stingray.common.actor.TryOrFailure]]
 */
class TryOrFailureSpecs
  extends Specification
  with ScalaCheck { def is = s2"""

  TryOrFailure is an implicit wrapper for Try objects to interoperate with Actors

  .orFailure should
    on a Try[A] that is a Success, return the A value                                      ${OrFailure.SuccessCase().ok}
    on a Try[A] that is a Failure, return a Status.Failure wrapping the failure exception  ${OrFailure.FailureCase().fails}

  .orFailureWith should
    on a Try[A] that is a Success, return the A value                                      ${OrFailureWith.SuccessCase().ok}
    on a Try[A] that is a Failure, return a Status.Failure wrapping the given exception    ${OrFailureWith.FailureCase().fails}

"""

  object OrFailure {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String]) { s =>
        Try { s }.orFailure must beEqualTo(s)
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Throwable]) { e =>
        Try { throw e }.orFailure must beEqualTo(Status.Failure(e))
      }
    }

  }

  object OrFailureWith {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String], arbitrary[Throwable]) { (s, e) =>
        Try { s }.orFailureWith(e) must beEqualTo(s)
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Throwable]) { e =>
        Try { throw new Throwable("incorrect") }.orFailureWith(e) must beEqualTo(Status.Failure(e))
      }
    }
  }

}
