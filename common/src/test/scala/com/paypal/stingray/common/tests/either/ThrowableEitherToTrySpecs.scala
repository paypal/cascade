package com.paypal.stingray.common.tests.either

import org.specs2._
import org.scalacheck.Prop.{Exception => PropException, _}
import org.scalacheck.Arbitrary._
import com.paypal.stingray.common.either._

/**
 * Tests for implicit [[com.paypal.stingray.common.either.ThrowableEitherToTry]]
 */
class ThrowableEitherToTrySpecs
  extends Specification
  with ScalaCheck { def is = s2"""

  ThrowableEitherToTry is an implicit wrapper to convert Either[Throwable, A] to Try[A]

  toTry should
    on a Left, return a Try Failure containing the Left exception ${LeftFailure().fails}
    on a Right, return a Try Success containing the Right value   ${RightSuccess().ok}

  """

  private case class CustomException(m: String) extends Exception(m)

  case class LeftFailure() {
    def fails = forAll(arbitrary[String]) { s =>
      Left(CustomException(s)).toTry must beFailedTry.withThrowable[CustomException](s)
    }
  }
  case class RightSuccess() {
    def ok = forAll(arbitrary[String]) { s =>
      Right(s).toTry must beSuccessfulTry.withValue(s)
    }
  }
}
