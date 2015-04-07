/**
 * Copyright 2013-2015 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common.tests.logging

import com.paypal.cascade.common.logging._
import org.specs2._

/**
 * Tests for [[com.paypal.cascade.common.logging.LoggingSugar]]
 */
class LoggingSugarSpecs extends Specification { override def is = s2"""

  Convenience methods for interacting with org.slf4j.Logger and other SLF4J objects.

  getLogger should retrieve an instance for the specified class       ${GetLogger().ok}

  """

  case class GetLogger() extends LoggingSugar {
    def ok = {
      val logger = getLogger[GetLogger]
      (logger must not beNull) and (logger must beAnInstanceOf[org.slf4j.Logger])
    }
  }

}
