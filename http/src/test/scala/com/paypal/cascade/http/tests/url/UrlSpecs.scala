/**
 * Copyright 2013-2014 PayPal
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
package com.paypal.cascade.http.tests.url

import org.specs2._
import com.paypal.cascade.http.url._
import java.net.URL

/**
 * Tests [[com.paypal.cascade.http.url]]
 */
class UrlSpecs extends Specification { override def is = s2"""

  RichURL provides methods to convert query strings to lists/maps

  queryList properly translates query string to a list            ${QueryString().listOk}
  queryPairs properly translates query string to a map            ${QueryString().mapOk}

  """

  case class QueryString() {
    def listOk = {
      val u = new URL("http://www.paypal.com/endpoint?key=value")
      u.queryList  must beEqualTo(List(("key", "value")))
    }
    def mapOk = {
      val u = new URL("http://www.paypal.com/endpoint?key=value")
      u.queryPairs  must beEqualTo(Map("key" -> List("value")))
    }
  }

}
