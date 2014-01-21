package com.paypal.stingray.common

import java.net.URL
import com.paypal.stingray.http.util.HttpUtil

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.http.url
 *
 * User: aaron
 * Date: 7/13/12
 * Time: 1:37 PM
 */

package object url {

  type StrPair = (String, String)

  implicit class RichURL(url: URL) {
    def queryList: List[StrPair] = HttpUtil.parseQueryStringToPairs(url.getQuery)
    def queryPairs: Map[String, List[String]] = HttpUtil.parseQueryStringToMap(url.getQuery)
  }

}
