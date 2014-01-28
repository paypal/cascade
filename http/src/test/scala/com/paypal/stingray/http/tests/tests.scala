package com.paypal.stingray.http

import org.scalacheck.Gen
import com.paypal.stingray.http.server.exception.ServiceException

/**
 * Convenience methods for testing services
 */
package object tests {

  def genServiceError[T <: ServiceException](implicit m: Manifest[T]): Gen[T] = {
    Gen.alphaStr.map(s => m.runtimeClass.getConstructor(classOf[String], classOf[Option[Throwable]]).newInstance(s, None).asInstanceOf[T])
  }

  def genServiceErrors[T <: ServiceException : Manifest]: Gen[List[T]] = Gen.nonEmptyListOf(genServiceError)
}
