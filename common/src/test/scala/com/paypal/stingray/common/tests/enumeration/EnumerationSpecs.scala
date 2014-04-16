package com.paypal.stingray.common.tests.enumeration

import com.paypal.stingray.common.enumeration._
import org.specs2.Specification
import scala.util.Try
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

/**
 * Tests for [[com.paypal.stingray.common.enumeration.Enumeration]]
 */
class EnumerationSpecs extends Specification { override def is = s2"""

  Tests our own type-safe Enumeration framework

  readEnum should return Some if the string is a valid enum           ${readEnum().returnsSome}
  readEnum should return None if the string is an invalid enum        ${readEnum().returnsNone}
  toEnum should return the enum value if the string is a valid enum   ${toEnum().returns}
  toEnum should throw if the string is an invalid enum value          ${toEnum().throws}

  """

  class Context extends CommonImmutableSpecificationContext {
    sealed abstract class MyEnum extends Enumeration
    object MyEnum1 extends MyEnum {
      override lazy val stringVal = "myenum1"
    }
    object MyEnum2 extends MyEnum {
      override lazy val stringVal = "myenum2"
    }
    implicit val reader = lowerEnumReader(MyEnum1, MyEnum2)
  }

  case class readEnum() extends Context {
    def returnsSome = apply {
      MyEnum1.stringVal.readEnum[MyEnum] must beSome.like {
        case e => e must beEqualTo(MyEnum1)
      }
    }
    def returnsNone = apply {
      s"${MyEnum1.stringVal}-INVALID".readEnum[MyEnum] must beNone
    }
  }

  case class toEnum() extends Context {
    def returns = apply {
      MyEnum1.stringVal.toEnum[MyEnum] must beEqualTo(MyEnum1)
    }
    def throws = apply {
      Try(s"${MyEnum1.stringVal}-INVALID".toEnum[MyEnum]).toOption must beNone
    }
  }

}
