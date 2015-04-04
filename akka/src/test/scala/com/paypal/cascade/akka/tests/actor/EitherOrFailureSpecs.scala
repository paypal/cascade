/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.akka.tests.actor

import org.specs2._
import org.scalacheck.Prop.{Exception => PropException, _}
import org.scalacheck.Arbitrary._
import com.paypal.cascade.akka.actor._
import com.paypal.cascade.common.tests.scalacheck._
import akka.actor._

/**
 * Tests for implicit [[com.paypal.cascade.akka.actor.EitherOrFailure]]
 */
class EitherOrFailureSpecs
  extends Specification
  with ScalaCheck { override def is = s2"""

  EitherOrFailure is an implicit wrapper for Either objects to interoperate with Actors

  .orFailureWith should, given an Exception e
    on an Either[E, A] that is Right, return the A value                          ${OrFailureWith.SuccessCase().ok}
    on an Either[E, A] that is Left, return Status.Failure(e)                     ${OrFailureWith.FailureCase().fails}

  .orFailureWith should, given a function E => Exception
    on an Either[E, A] that is Right, return the A value                          ${OrFailureWithConversion.SuccessCase().ok}
    on an Either[E, A] that is Left, return Status.Failure with a converted left  ${OrFailureWithConversion.FailureCase().fails}

  """

  object OrFailureWith {

    case class SuccessCase() {
      def ok = forAll(arbitrary[String], arbitrary[Exception]) { (s, e) =>
        Right(s).orFailureWith(e) must beEqualTo(s)
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[Exception]) { e =>
        Left(new Exception("incorrect")).orFailureWith(e) must beEqualTo(Status.Failure(e))
      }
    }
  }

  object OrFailureWithConversion {

    private case class ConvertedException(m: String) extends Exception(m)

    case class SuccessCase() {
      def ok = forAll(arbitrary[Int]) { i =>
        Right[String, Int](i).orFailureWith(new Exception(_)) must beEqualTo(i)
      }
    }
    case class FailureCase() {
      def fails = forAll(arbitrary[String]) { s =>
        Left[String, Int](s).orFailureWith(ConvertedException) must beEqualTo(Status.Failure(ConvertedException(s)))
      }
    }
  }

}
