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
import scala.util.Try
import com.paypal.cascade.common.trys._

/**
 * Tests [[com.paypal.cascade.common.trys]]
 */
class TrysSpecs extends Specification with ScalaCheck { override def is = s2"""

  Convenience wrappers and methods for working with [[scala.util.Try]].

  Try[A] returning value should be converted and return right           ${TryToEither().successCase}
  Try[A] returning exception should be converted and return left        ${TryToEither().errorCase}


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

}
