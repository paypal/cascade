package com.paypal.stingray.http.tests.url

import org.specs2._
import com.paypal.stingray.http.url._
import java.net.URL

/**
 * Tests [[com.paypal.stingray.http.url]]
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
