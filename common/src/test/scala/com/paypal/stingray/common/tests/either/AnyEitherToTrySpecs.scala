package com.paypal.stingray.common.tests.either

import org.specs2._
import org.scalacheck.Prop.{Exception => PropException, _}
import org.scalacheck.Arbitrary._
import com.paypal.stingray.common.either._

/**
 * Tests for implicit [[com.paypal.stingray.common.either.AnyEitherToTry]]
 */
class AnyEitherToTrySpecs
  extends Specification
  with ScalaCheck { def is = s2"""

  AnyEitherToTry is an implicit wrapper to convert Either[E, A] to Try[A]

  toTry, given a conversion function E => Exception
    on a Left, return a Try Failure containing the converted Left exception ${LeftFailure().fails}
    on a Right, return a Try Success containing the Right value             ${RightSuccess().ok}

  """

  private case class CustomException(m: String) extends Exception(m)

  case class LeftFailure() {
    def fails = forAll(arbitrary[Int]) { i =>
      Left(i).toTry(intV => CustomException(intV.toString)) must beFailedTry.withThrowable[CustomException](i.toString)
    }
  }
  case class RightSuccess() {
    def ok = forAll(arbitrary[String]) { s =>
      (Right(s): Either[Int, String]).toTry(intV => CustomException(intV.toString)) must beSuccessfulTry.withValue(s)
    }
  }
}
