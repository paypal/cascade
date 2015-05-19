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
package com.paypal.cascade.common.tests.enumeration

import com.paypal.cascade.common.enumeration._
import org.specs2.Specification
import scala.util.Try
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.cascade.common.enumeration.Enumeration]]
 */
class EnumerationSpecs extends Specification { override def is = s2"""

  Tests our own type-safe Enumeration framework

  LowerEnumReader:

  readEnum should return Some if the string is a valid enum           ${LowerReadEnum().returnsSome}
  readEnum should return None if the string is an invalid enum        ${LowerReadEnum().returnsNone}
  toEnum should return the enum value if the string is a valid enum   ${LowerToEnum().returns}
  toEnum should throw if the string is an invalid enum value          ${LowerToEnum().throws}

  UpperEnumReader:

  readEnum should return Some if the string is a valid enum           ${UpperReadEnum().returnsSome}
  readEnum should return None if the string is an invalid enum        ${UpperReadEnum().returnsNone}
  toEnum should return the enum value if the string is a valid enum   ${UpperToEnum().returns}
  toEnum should throw if the string is an invalid enum value          ${UpperToEnum().throws}

  """

  class LowerContext extends CommonImmutableSpecificationContext {
    sealed abstract class MyLowerEnum extends Enumeration
    object MyLowerEnum1 extends MyLowerEnum {
      override lazy val stringVal = "myenum1"
    }
    object MyLowerEnum2 extends MyLowerEnum {
      override lazy val stringVal = "myenum2"
    }
    implicit val reader = lowerEnumReader(MyLowerEnum1, MyLowerEnum2)
  }

  class UpperContext extends CommonImmutableSpecificationContext {
    sealed abstract class MyUpperEnum extends Enumeration
    object MyUpperEnum1 extends MyUpperEnum {
      override lazy val stringVal = "MYENUM1"
    }
    object MyUpperEnum2 extends MyUpperEnum {
      override lazy val stringVal = "MYENUM2"
    }
    implicit val reader = upperEnumReader(MyUpperEnum1, MyUpperEnum2)
  }

  case class LowerReadEnum() extends LowerContext {
    def returnsSome = apply {
      MyLowerEnum1.stringVal.readEnum[MyLowerEnum] must beSome.like {
        case e => e must beEqualTo(MyLowerEnum1)
      }
    }
    def returnsNone = apply {
      s"${MyLowerEnum1.stringVal}-INVALID".readEnum[MyLowerEnum] must beNone
    }
  }

  case class LowerToEnum() extends LowerContext {
    def returns = apply {
      MyLowerEnum1.stringVal.toEnum[MyLowerEnum] must beEqualTo(MyLowerEnum1)
    }
    def throws = apply {
      Try(s"${MyLowerEnum1.stringVal}-INVALID".toEnum[MyLowerEnum]).toOption must beNone
    }
  }

  case class UpperReadEnum() extends UpperContext {
    def returnsSome = apply {
      MyUpperEnum1.stringVal.readEnum[MyUpperEnum] must beSome.like {
        case e => e must beEqualTo(MyUpperEnum1)
      }
    }
    def returnsNone = apply {
      s"${MyUpperEnum1.stringVal}-INVALID".readEnum[MyUpperEnum] must beNone
    }
  }

  case class UpperToEnum() extends UpperContext {
    def returns = apply {
      MyUpperEnum1.stringVal.toEnum[MyUpperEnum] must beEqualTo(MyUpperEnum1)
    }
    def throws = apply {
      Try(s"${MyUpperEnum1.stringVal}-INVALID".toEnum[MyUpperEnum]).toOption must beNone
    }
  }

}
