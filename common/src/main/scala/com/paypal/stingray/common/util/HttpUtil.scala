package com.paypal.stingray.common.util

import scalaz._
import Scalaz._
import java.net.URLDecoder

object HttpUtil {
  import com.paypal.stingray.common.url.StrPair

  val UTF_8 = "utf-8"

  /**
   * parse a query string into a list of key-value pairs
   * @param queryString the query string to parse (without the '?')
   * @return a list of (String, String), representing each key-value pair in the query string
   */
  def parseQueryStringToPairs(queryString: String): List[StrPair] = {
    val queryStringPieces: List[String] = (~Option(queryString)).split("&").toList
    queryStringPieces.flatMap { piece: String =>
      piece.split("=").toList match {
        case key :: value :: Nil if(key.length > 0 && value.length > 0) => List(URLDecoder.decode(key, UTF_8) -> URLDecoder.decode(value, UTF_8))
        case _ => List()
      }
    }.toList
  }

  /**
   * Parse a query string into a map of key/value pairs. ignores invalid key-value pairs in the query string
   *
   * @param queryString the query string to parse, without the leading '?'
   * @return key/value pairs mapping to the items in the query string
   */
  def parseQueryStringToMap(queryString: String): Map[String, List[String]] = {
    val pairs = parseQueryStringToPairs(queryString)

    pairs.foldLeft(Map[String, List[String]]()) { (running, current) =>
      val (curKey, curVal) = current
      if(curKey.length > 0 && curVal.length > 0) {
        val newList = running.get(curKey).map { existingList =>
          existingList ++ List(curVal)
        } | List(curVal)
        running ++ Map(curKey -> newList)
      } else {
        running
      }
    }
  }

  val FormURLEncodedContentType = "application/x-www-form-urlencoded"

  /**
   * Parse the parameters from a query string, and append the parameters
   * specified in a POST message body if the Content-Type is
   * "application/x-www-form-urlencoded"
   *
   * @param queryString Query string part of the url
   * @param body Post message body
   * @param headers request headers
   * @return Map of parameter names to list of given values.
   */
  def parseQueryStringAndBody(queryString: String, body: String, headers: Map[String, String]): Map[String, List[String]] = {
    val params: Map[String, List[String]] = parseQueryStringToMap(queryString)
    (Option(headers) map (_.get("content-type"))).join match {
      case Some(contentType) if(contentType.startsWith(FormURLEncodedContentType)) => {
        mergeParameters(params, parseQueryStringToMap(body))
      }
      case _ => {
        params
      }
    }
  }

  def mergeParameters(m1: Map[String, List[String]], m2: Map[String, List[String]]): Map[String, List[String]] = {
    val list: List[(String, List[String])] = m1.toList ++ m2.toList
    list.foldLeft(Map[String, List[String]]()) { (runningMap, currentElt) =>
      val (currentKey, currentList) = currentElt
      val newList = runningMap.get(currentKey).map { existingList =>
        existingList ++ currentList
      } | currentList
      runningMap ++ Map(currentKey -> newList)
    }
  }
}
