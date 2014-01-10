package com.stackmob.tests.common.crypto

import org.specs2._
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import com.stackmob.tests.common.util.CommonImmutableSpecificationContext
import com.paypal.stingray.common.crypto._

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 10/2/12
 * Time: 6:21 PM
 */

class CryptoSpecs
  extends Specification
  with ScalaCheck { override def is =

  "CryptoSpecs".title                                                  ^
  """
  The crypto object is used to encrypt and decrypt values.
  """                                                                  ^
                                                                       p^
  "Encrypting a string should"                                         ^
    "Decrypt to the original value"                                    ! encryptor().ok ^
                                                                       end

  case class encryptor() extends CommonImmutableSpecificationContext {

    def ok = apply {
      forAll(arbitrary[String]) { s =>
        decrypt(encrypt(s)) must beEqualTo(s)
      }
    }

  }

}
