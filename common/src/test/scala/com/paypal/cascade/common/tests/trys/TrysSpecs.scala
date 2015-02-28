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
package com.paypal.cascade.common.tests.trys

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import scala.util.{Failure, Success, Try}
import com.paypal.cascade.common.trys._

/**
 * Tests [[com.paypal.cascade.common.trys]]
 */
class TrysSpecs extends Specification with ScalaCheck { override def is = s2"""

  Convenience wrappers and methods for working with [[scala.util.Try]].

  Try[A] returning value should be converted and return right           ${TryToEither().successCase}
  Try[A] returning exception should be converted and return left        ${TryToEither().errorCase}

  sequenceOptionTry should
    Return Success(Some)                                                ${OptionTry().successSome}
    Return Success(None)                                                ${OptionTry().successNone}
    Returns Failure                                                     ${OptionTry().failure}

  sequence should
    return a successful list                                            ${ListTry().allSuccess}
    return a failure                                                    ${ListTry().someFailure}
    return a successful empty list                                      ${ListTry().empty}
  """

  case class TryToEither() {
    def successCase = forAll(arbitrary[String]) { str =>
      val e = Try[String] { str }.toEither
      e must beRight.like {
        case ex: String => ex must beEqualTo(str)
      }
    }

    def errorCase = forAll(arbitrary[Throwable]) { th =>
      val e = Try[String] { throw th }.toEither
      e must beLeft.like {
        case ex: Throwable => ex.getMessage must beEqualTo(th.getMessage)
      }
    }

  }

  case class OptionTry() {
    def successSome = forAll(arbitrary[String]) { str =>
      val e: Option[Try[String]] = Option(Try { str })
      e.sequence must beEqualTo(Success(Some(str)))
    }

    def successNone = {
      val o: Option[Try[String]] = None
      o.sequence must beEqualTo(Success(None))
    }

    def failure = {
      val re = new RuntimeException("Ouch!")
      val e: Option[Try[String]] = Option(Failure(re))
      e.sequence must beEqualTo(Failure(re))
    }
  }

  case class ListTry() {

    def allSuccess = {
      val l: List[Try[Int]] = List(Success(1), Success(2))
      l.sequence must beEqualTo(Success(List(1,2)))
    }

    def someFailure = {
      val ex = new RuntimeException("Ouch!")
      val l: List[Try[Int]] = List(Success(1), Failure(ex))
      l.sequence must beEqualTo(Failure(ex))
    }

    def empty = {
      val l: List[Try[Int]] = List.empty
      l.sequence must beEqualTo(Success(List.empty))
    }

  }

}
