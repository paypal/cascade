package com.paypal.stingray.common.tests.trys

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import scala.util.{Try, Either}
import java.net.URL
import com.paypal.stingray.common.trys._

/**
 * Tests [[com.paypal.stingray.common.trys]]
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
