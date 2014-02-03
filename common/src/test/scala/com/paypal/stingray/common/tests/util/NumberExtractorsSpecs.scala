package com.paypal.stingray.common.tests.util

import org.specs2._
import org.scalacheck._
import com.paypal.stingray.common.util.{IntExtractor, LongExtractor}

/**
 * Tests number extractor methods [[com.paypal.stingray.common.util]]
 */
class NumberExtractorsSpecs extends Specification with ScalaCheck { def is = s2"""

  Convenience methods for converting String to Int/Long.

  For IntExtractor:
    Int is returned when String is successfully converted       ${IntExtract().successCase}
    0 is returned when conversion returns None                  ${IntExtract().failureCase}

  For LongExtractor:
    Long is returned when String is successfully converted      ${LongExtract().successCase}
    0 is returned when conversion returned None                 ${LongExtract().failureCase}


"""

  case class IntExtract() {
    def successCase = {
      val newInt = "1234" match {
        case IntExtractor(a) => a
        case _ => 0
      }
      (newInt must beEqualTo(1234)) and
        (newInt must beAnInstanceOf[java.lang.Integer])
    }
    def failureCase = {
      val newInt = "1234a" match {
        case IntExtractor(a) => a
        case _ => 0
      }
      (newInt must beEqualTo(0)) and
        (newInt must beAnInstanceOf[java.lang.Integer])
    }
  }

  case class LongExtract() {
    def successCase = {
      val newLong = "1234" match {
        case LongExtractor(a) => a
        case _ => 0L
      }
      (newLong must beEqualTo(1234)) and
        (newLong must beAnInstanceOf[java.lang.Long])
    }
    def failureCase = {
      val newLong = "1234a" match {
        case LongExtractor(a) => a
        case _ => 0L
      }
      (newLong must beEqualTo(0L)) and
        (newLong must beAnInstanceOf[java.lang.Long])
    }
  }

}
