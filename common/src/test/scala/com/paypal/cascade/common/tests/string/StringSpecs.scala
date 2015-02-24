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
package com.paypal.cascade.common.tests.string

import java.nio.charset.StandardCharsets.UTF_8

import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.specs2._

import com.paypal.cascade.common.string.RichString

/**
 * Tests for [[com.paypal.cascade.common.string]]
 */
class StringSpecs extends Specification with ScalaCheck { override def is = s2"""

  string.scala provides convenience methods and implicit wrappers for working with Strings

  string.getBytes should invoke implicit class RichString getBytesUTF8 method   ${GetBytes().ok}

  """

  case class GetBytes() {
    def ok = forAll(arbitrary[String]) { str =>
      val bytesArray = str.getBytesUTF8
      val decoded = new String(bytesArray, UTF_8)
      (bytesArray must beAnInstanceOf[Array[Byte]]) and
        (str must beEqualTo(decoded))
    }
  }

}
