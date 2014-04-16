package com.paypal.stingray.akka.tests.actor

import org.specs2._
import org.scalacheck.Prop.{Exception => PropException, _}
import org.scalacheck.Arbitrary._
import scala.util.Try
import com.paypal.stingray.akka.actor._
import com.paypal.stingray.common.tests.scalacheck._
import akka.actor._

/**
 * Tests for implicit [[com.paypal.stingray.akka.actor.TryOrFailure]]
 */
class TryOrFailureSpecs
  extends Specification
  with ScalaCheck { override def is = s2"""

  TryOrFailure is an implicit wrapper for Try objects to interoperate with Actors

  .orFailure should
    on a Try[A] that is a Success, return the A value                                       ${OrFailure.SuccessCase().ok}
    on a Try[A] that is a Failure, return a Status.Failure wrapping the failure exception   ${OrFailure.FailureCase().fails}
    on a Try[A] that is a Failure with an Error, throw the Error                            ${OrFailure.ErrorCase().failsHard}

  .orFailureWith should, given a concrete Exception
    on a Try[A] that is a Success, return the A value                                       ${OrFailureWith.SuccessCase().ok}
    on a Try[A] that is a Failure, return a Status.Failure wrapping the given exception     ${OrFailureWith.FailureCase().fails}
    on a Try[A] that is a Failure with an Error, throw the Error                            ${OrFailureWith.ErrorCase().failsHard}

  .orFailureWith should, given a conversion function Exception => Exception
    on a Try[A] that is a Success, return the A value                                       ${OrFailureWithConversion.SuccessCase().ok}
    on a Try[A] that is a Failure, return a Status.Failure wrapping the converted exception ${OrFailureWithConversion.FailureCase().fails}
    on a Try[A] that is a Failure with an Error, throw the Error                            ${OrFailureWithConversion.ErrorCase().failsHard}

  """

  object OrFailure {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String]) { s =>
        Try(s).orFailure must beEqualTo(s)
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Exception]) { e =>
        Try(throw e).orFailure must beEqualTo(Status.Failure(e))
      }
    }
    case class ErrorCase() {
      def failsHard = forAll(arbitrary[Error]) { err =>
        Try(throw err).orFailure must throwA[Error]
      }
    }
  }

  object OrFailureWith {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String], arbitrary[Exception]) { (s, e) =>
        Try(s).orFailureWith(e) must beEqualTo(s)
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Exception]) { e =>
        Try(throw new Exception("incorrect")).orFailureWith(e) must beEqualTo(Status.Failure(e))
      }
    }
    case class ErrorCase() {
      def failsHard = forAll(arbitrary[Error], arbitrary[Exception]) { (err, e) =>
        Try(throw err).orFailureWith(e) must throwA[Error]
      }
    }
  }

  object OrFailureWithConversion {

    private case class ConvertedException(e: Exception) extends Exception(s"converted: ${e.getMessage}")

    case class SuccessCase() {
      def ok = forAll(arbitrary[String]) { s =>
        Try(s).orFailureWith(ConvertedException(_)) must beEqualTo(s)
      }
    }

    case class FailureCase() {
      def fails = forAll(arbitrary[String], arbitrary[Exception]) { (s, e) =>
        Try(throw e).orFailureWith(ConvertedException(_)) must beEqualTo(Status.Failure(ConvertedException(e)))
      }
    }

    case class ErrorCase() {
      def failsHard = forAll(arbitrary[Error]) { err =>
        Try(throw err).orFailureWith(ConvertedException(_)) must throwA[Error]
      }
    }

  }

}
