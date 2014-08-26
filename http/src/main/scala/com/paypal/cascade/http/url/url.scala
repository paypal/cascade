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
package com.paypal.cascade.http

import java.net.URL
import com.paypal.cascade.http.util.HttpUtil

/**
 * Convenience type and implicits for working with [[java.net.URL]] objects
 */
package object url {

  /** Convenience type */
  type StrPair = (String, String)

  /**
   * Convenience implicit wrapper for URLs
   *
   * {{{
   *   import com.paypal.cascade.http.url._
   *   val u = new URL("http://www.paypal.com/endpoint?key=value")
   *   u.queryList  // List(("key", "value"))
   * }}}
   *
   * @param url the URL to wrap
   */
  implicit class RichURL(url: URL) {

    /**
     * Converts a query string to a List of String pairs
     * @return a list of String pairs
     */
    def queryList: List[StrPair] = HttpUtil.parseQueryStringToPairs(url.getQuery)

    /**
     * Converts a query string to a Map of String pairs
     * @return a map of String pairs
     */
    def queryPairs: Map[String, List[String]] = HttpUtil.parseQueryStringToMap(url.getQuery)

  }

}
