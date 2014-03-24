package com.paypal.stingray.common.tests.trys

import org.specs2._
import org.scalacheck.Prop.{Exception => PropException, _}
import org.scalacheck.Arbitrary._
import scala.util.Try
import com.paypal.stingray.common.trys._
import com.paypal.stingray.common.tests.scalacheck._

/**
 * RichTry is a convenience wrapper for converting Try to Either
 */
class RichTrySpecs
  extends Specification
  with ScalaCheck { override def is = s2"""

  RichTry is an implicit wrapper for Try objects

  toEither should
    on a Try[A] Success, return an Either[Throwable, A] Right with the Success value      ${ToEither.SuccessCase().ok}
    on a Try[A] Failure, return an Either[Throwable, A] Left with the Failure exception   ${ToEither.FailureCase().fails}

  toEither[LeftT] should
    on a Try[A] Success, return an Either[LeftT, A] Right with the Success value          ${ToEitherWithConversion.SuccessCase().ok}
    on a Try[A] Failure, return an Either[LeftT, A] Left with the converted Failure value ${ToEitherWithConversion.FailureCase().fails}

  """

  object ToEither {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String]) { s =>
        Try { s }.toEither must beRight.like { case v: String =>
          v must beEqualTo(s)
        }
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Exception]) { e =>
        Try[String] { throw e }.toEither must beLeft.like { case v: Exception =>
          v must beEqualTo(e)
        }
      }
    }
  }

  object ToEitherWithConversion {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String]) { s =>
        Try { s }.toEither[Int](_.getMessage.length) must beRight.like { case v: String =>
          v must beEqualTo(s)
        }
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Exception], arbitrary[Int]) { (e, i) =>
        Try[String] { throw e }.toEither[Int](_ => i) must beLeft.like { case v: Int =>
          v must beEqualTo(i)
        }
      }
    }
  }

}
