package com.paypal.stingray.http.tests.server

import com.paypal.stingray.http.server.exception.ServiceException
import org.specs2._

/**
 * Tests for [[com.paypal.stingray.http.server.exception.ServiceException]]
 */
class ServiceExceptionSpecs extends Specification with ScalaCheck { def is=s2"""

  ServiceException works correctly          ${ServiceExceptionTest().ok}

"""

  case class ServiceExceptionTest() {
    case class SomeServiceException(message: String) extends ServiceException(message)
    def ok = {
      val ex = new SomeServiceException("fail")
      ex must beAnInstanceOf[Exception]
    }
  }
}
