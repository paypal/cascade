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
package com.paypal.stingray.common.tests.string

import org.specs2._
import org.scalacheck.Prop._
import com.paypal.stingray.common.string.RichString
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import org.scalacheck.Arbitrary._

/**
 * Tests for [[com.paypal.stingray.common.string]]
 */
class StringSpecs extends Specification with ScalaCheck { override def is = s2"""

  string.scala provides convenience methods and implicit wrappers for working with Strings

  string.getBytes should invoke implicit class RichString getBytesUTF8 method   ${GetBytes().ok}

  """

  case class GetBytes() {
    def ok = forAll(arbitrary[String]) { str =>
      val bytesArray = str.getBytesUTF8
      val decoded = new String(bytesArray, charsetUtf8)
      (bytesArray must beAnInstanceOf[Array[Byte]]) and
        (str must beEqualTo(decoded))
    }
  }

}
