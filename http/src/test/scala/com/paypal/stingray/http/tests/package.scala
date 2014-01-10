package com.paypal.stingray.http

import org.scalacheck.Gen
import org.scalacheck.Arbitrary._
import java.util.UUID
import com.stackmob.tests.common.scalacheck.Generators
import server.exception.ServiceException

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.tests
 *
 * User: aaron
 * Date: 7/5/12
 * Time: 2:37 PM
 */

package object tests extends Generators {
  import Gen._

  def genServiceError[T <: ServiceException](implicit m: Manifest[T]): Gen[T] = {
    Gen.alphaStr.map(s => m.runtimeClass.getConstructor(classOf[String], classOf[Option[Throwable]]).newInstance(s, None).asInstanceOf[T])
  }

  def genServiceErrors[T <: ServiceException : Manifest]: Gen[List[T]] = Gen.listOf1(genServiceError)
}
