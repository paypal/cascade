package com.paypal.stingray.common.tests.constants

import org.specs2._
import com.paypal.stingray.common.constants.ValueConstants.charsetUtf8
import java.lang.String
import org.scalacheck.Prop._
import org.scalacheck.Arbitrary._

/**
 * Tests for [[com.paypal.stingray.common.constants.ValueConstants]]
 */
class ValueConstantsSpecs extends Specification with ScalaCheck { override def is = s2"""

ValueConstants are for commonly used constants and strings

charsetUtf8 should represent Charset.getName("UTF-8")         ${ValueConstants.CharsetUtf8().ok}

"""

  object ValueConstants {
    case class CharsetUtf8() {
      def ok = forAll(arbitrary[String]){ originalString =>
        val originalStringBytes = originalString.getBytes(charsetUtf8)
        val newString = new String(originalStringBytes, charsetUtf8)
        newString must beEqualTo(originalString)
      }
    }
  }

}
