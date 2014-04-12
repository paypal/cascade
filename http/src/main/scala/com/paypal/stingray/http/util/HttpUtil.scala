package com.paypal.stingray.http.util

import java.net.URLDecoder

/**
 * Convenience methods for interacting with URLs and other request components.
 *
 * Methods found within [[spray.http]] objects should be preferred over these, wherever Spray objects are already
 * in use. For example, if working with a [[spray.http.Uri]], prefer to access its query string pairs using
 * [[spray.http.Uri.Query.toMap]] instead of using `parseQueryStringToMap` found here.
 */
object HttpUtil {
  import com.paypal.stingray.http.url.StrPair

  /** Convenience value for `utf-8` */
  val UTF_8 = "utf-8"

  val CONTENT_LANGUAGE = "Content-Language"
  val CONTENT_LANGUAGE_LC = CONTENT_LANGUAGE.toLowerCase

  /**
   * Parse a query string into a List of key-value String pairs
   *
   * @param queryString the query string to parse (without the '?')
   * @return a list of (String, String), representing each key-value pair in the query string
   */
  def parseQueryStringToPairs(queryString: String): List[StrPair] = {
    val queryStringPieces: List[String] = Option(queryString).map(_.split("&").toList).getOrElse(List())
    queryStringPieces.flatMap { piece: String =>
      piece.split("=").toList match {
        case key :: value :: Nil if key.length > 0 && value.length > 0 => List(URLDecoder.decode(key, UTF_8) -> URLDecoder.decode(value, UTF_8))
        case _ => List()
      }
    }.toList
  }

  /**
   * Parse a query string into a Map of key-value String pairs. Ignores invalid key-value pairs in the query string
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
        }.getOrElse(List(curVal))
        running ++ Map(curKey -> newList)
      } else {
        running
      }
    }
  }

  /**
   * Merge two parameter maps into one
   * @param m1 the first map
   * @param m2 the second map
   * @return a merged map containing members from both maps
   */
  def mergeParameters(m1: Map[String, List[String]],
                      m2: Map[String, List[String]]): Map[String, List[String]] = {
    val list: List[(String, List[String])] = m1.toList ++ m2.toList
    list.foldLeft(Map[String, List[String]]()) { (runningMap, currentElt) =>
      val (currentKey, currentList) = currentElt
      val newList = runningMap.get(currentKey).map { existingList =>
        existingList ++ currentList
      }.getOrElse(currentList)
      runningMap ++ Map(currentKey -> newList)
    }
  }
}
