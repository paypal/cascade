package com.paypal.stingray.http

import java.net.URL
import com.paypal.stingray.http.util.HttpUtil

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
   *   import com.paypal.stingray.http.url._
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
