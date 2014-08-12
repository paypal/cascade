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
package com.paypal.stingray.common.tests.either

import org.specs2._
import com.paypal.stingray.common.either._
import org.scalacheck.Prop._
import scala.Exception
import org.scalacheck.Arbitrary._

/**
 * Tests implicit classes in [[com.paypal.stingray.common.either]]
 */
class EitherSpecs extends Specification with ScalaCheck { def is =s2"""

  AnyEitherToTry is an implicit wrapper to convert Either[E, A] to Try[A]

  toTry, given a conversion function E => Exception
    on a Left, return a Try Failure containing the converted Left exception                 ${AnyEitherToLeft().fails}
    on a Right, return a Try Success containing the Right value                             ${AnyEitherToRight().ok}

  EitherOps is an implicit wrapper to convert regular objects to [[scala.util.Either]]

  toRight creates an either with the object as the right                                    ${EitherOpsToRight().success}
  toLeft creates an either with the object as the left                                      ${EitherOpsToLeft().success}

  ThrowableEitherToTry is an implicit wrapper to convert Either[Throwable, A] to Try[A]

  toTry should
    on a Left, return a Try Failure containing the Left exception                           ${ThrowableEitherToLeft().fails}
    on a Right, return a Try Success containing the Right value                             ${ThrowableEitherToRight().ok}

  """

  private case class CustomException(m: String) extends Exception(m)

  case class AnyEitherToLeft() {
    def fails = forAll(arbitrary[Int]) { i =>
      Left(i).toTry(intV => CustomException(intV.toString)) must beFailedTry.withThrowable[CustomException](i.toString)
    }
  }
  case class AnyEitherToRight() {
    def ok = forAll(arbitrary[String]) { s =>
      (Right(s): Either[Int, String]).toTry(intV => CustomException(intV.toString)) must beSuccessfulTry.withValue(s)
    }
  }

  case class EitherOpsToRight() {
    def success = {
      "hello".toRight[Throwable] must beAnInstanceOf[Either[Throwable, String]]
    }
  }

  case class EitherOpsToLeft() {
    def success = {
      "hello".toLeft[Int] must beAnInstanceOf[Either[String, Int]]
    }
  }

  case class ThrowableEitherToLeft() {
    def fails = forAll(arbitrary[String]) { s =>
      Left(CustomException(s)).toTry must beFailedTry.withThrowable[CustomException]
    }
  }

  case class ThrowableEitherToRight() {
    def ok = forAll(arbitrary[String]) { s =>
      Right(s).toTry must beSuccessfulTry.withValue(s)
    }
  }

}
