package com.paypal.stingray.common.tests.logging

import com.paypal.stingray.common.logging._
import org.specs2._

/**
 * Tests for [[com.paypal.stingray.common.logging.LoggingSugar]]
 */
class LoggingSugarSpecs extends Specification { def is = s2"""

  Convenience methods for interacting with org.slf4j.Logger and other SLF4J objects.

  getLogger should retrieve an instance for the specified class       ${Logger.GetLogger().ok}

  """

  object Logger {

    case class GetLogger() extends LoggingSugar {

      def ok = {
        val logger = getLogger[GetLogger]
        (logger must not beNull) and
          (logger must beAnInstanceOf[org.slf4j.Logger])
      }

    }
  }
}
