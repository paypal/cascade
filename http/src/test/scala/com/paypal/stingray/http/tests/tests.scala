package com.paypal.stingray.http

import org.scalacheck.Gen
import com.paypal.stingray.http.server.exception.ServiceException
import scala.reflect.ClassTag

/**
 * Convenience methods for testing services
 */
package object tests {

  /**
   * Generates an arbitrary [[ServiceException]]
   * @param c implicitly, the ClassTag of this ServiceException
   * @tparam T the type of this ServiceException
   * @return an arbitrary ServiceException
   */
  def genServiceError[T <: ServiceException](implicit c: ClassTag[T]): Gen[T] = {
    Gen.alphaStr.map(s => c.runtimeClass.getConstructor(classOf[String], classOf[Option[Throwable]]).newInstance(s, None).asInstanceOf[T])
  }

  /**
   * Generates a List of ServiceExceptions
   * @tparam T the type of ServiceExceptions generated
   * @return a List of ServiceExceptions
   */
  def genServiceErrors[T <: ServiceException : ClassTag]: Gen[List[T]] = Gen.nonEmptyListOf(genServiceError)
}
