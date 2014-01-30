package com.paypal.stingray.http.tests.util

import org.specs2.{ScalaCheck, Specification}
import org.specs2.execute.{Result => SpecsResult}
import com.paypal.stingray.common.logging.LoggingSugar
import org.scalacheck.Gen
import org.scalacheck.Prop._
import org.scalacheck.Gen._
import java.net.URLDecoder
import com.paypal.stingray.http.url.StrPair
import com.paypal.stingray.http.util.HttpUtil

/**
 * Tests features of [[com.paypal.stingray.http.util.HttpUtil]]
 */
class HttpUtilSpecs extends Specification with ScalaCheck { def is =
  "HttpUtilSpecs".title                                                                                                 ^
  """
  HttpUtil contains a variety of different methods for doing common HTTP related tasks, such as parsing query strings
  """                                                                                                                   ^
  "HttpUtil#parseQueryStringToPairs should"                                                                             ^
    "parse a well formed query string to valid pairs"                                                                   ! ParseQueryStringToPairs().succeedsForWellFormedString ^
    "parse a well formed query string with url encoded values to valid pairs"                                           ! ParseQueryStringToPairs().succeedsForURLEncodedString ^
    "parse a null string"                                                                                               ! ParseQueryStringToPairs().succeedsForNullString ^
    "omit malformed query string key-value pairs"                                                                       ! ParseQueryStringToPairs().omitsMalformedKVPs ^
                                                                                                                        end ^
  "HttpUtil#parseQueryStringToMap should"                                                                               ^
    "parse a well formed query string to valid pairs"                                                                   ! ParseQueryStringToMap().succeedsForWellFormedString ^
                                                                                                                        end ^
  "HttpUtil#mergeParameters should"                                                                                     ^
    "merge two valid maps together when keys don't overlap"                                                             ! MergeParameters().mergesNonOverlappingKeys ^
    "merge two valid maps together when keys overlap"                                                                   ! MergeParameters().mergesOverlappingKeys ^
    "merge an empty map with a non-empty one"                                                                           ! MergeParameters().mergesEmptyMap ^
                                                                                                                        end
  trait Context extends LoggingSugar {
    protected lazy val genNonEmptyAlphaStr = alphaStr.suchThat(_.length > 0)
    protected lazy val genMalformedQueryStringPair = genNonEmptyAlphaStr.suchThat { s =>
      !s.contains("=") && !s.contains("&")
    }
    protected lazy val genNonEmptyStrPair: Gen[StrPair] = for {
      key <- genNonEmptyAlphaStr
      value <- genNonEmptyAlphaStr
    } yield (key, value)

    protected lazy val genQueryPairs: Gen[List[StrPair]] = listOf(genNonEmptyStrPair)

    protected def getQueryString(l: List[StrPair]) = l.map(tup => s"${tup._1}=${tup._2}").mkString("&")

    protected def allValues[T](m: Map[String, List[T]]): List[T] = m.flatMap(tup => tup._2).toList
    protected def uniqueKeys[T, U](maps: Map[T, U]*): Set[T] = maps.toList.foldLeft(Set[T]()) { (set, map) =>
      set ++ map.keys.toList
    }
  }

  case class ParseQueryStringToPairs() extends Context {
    def succeedsForWellFormedString = forAll(genQueryPairs) { list =>
      val qString = getQueryString(list)
      HttpUtil.parseQueryStringToPairs(qString) must containTheSameElementsAs(list)
    }

    def succeedsForURLEncodedString = forAll(genQueryPairs) { list =>
      val encoded = ("redirect_uri", "http%3A%2F%2Fstackmob.com")
      val decoded = (URLDecoder.decode(encoded._1, HttpUtil.UTF_8), URLDecoder.decode(encoded._2, HttpUtil.UTF_8))
      val qString = getQueryString(encoded :: list)
      HttpUtil.parseQueryStringToPairs(qString) must containTheSameElementsAs(decoded :: list)
    }

    def succeedsForNullString = {
      HttpUtil.parseQueryStringToPairs(null) must containTheSameElementsAs(List[StrPair]())
    }

    def omitsMalformedKVPs = forAll(genQueryPairs, genMalformedQueryStringPair) { (list, malformedStr) =>
      val qStringWithMalformed = s"${getQueryString(list)}&${malformedStr}"
      HttpUtil.parseQueryStringToPairs(qStringWithMalformed) must containTheSameElementsAs(list)
    }
  }

  case class ParseQueryStringToMap() extends Context {
    def succeedsForWellFormedString = forAll(genQueryPairs) { list =>
      val qString = getQueryString(list)
      val parsed = HttpUtil.parseQueryStringToMap(qString)

      val parsedKeys: List[String] = parsed.keys.toList
      //since there can be duplicate keys, create a map of key->"", then convert back to a list of the keys.
      //the toMap method will de-duplicate keys
      val expectedKeys: List[String] = list.map(_._1 -> "").toMap.keys.toList

      parsedKeys.length must beEqualTo(expectedKeys.length)
    }
  }

  case class MergeParameters() extends Context {
    def mergesNonOverlappingKeys: SpecsResult = {
      val map1 = Map("a" -> List("b", "c"))
      val map2 = Map("b" -> List("c", "d"))
      val merged = HttpUtil.mergeParameters(map1, map2)
      val keysMatch = merged.keys must containTheSameElementsAs(uniqueKeys(map1, map2).toSeq)
      val valsMatch = allValues(merged) must containTheSameElementsAs(allValues(map1) ++ allValues(map2))
      keysMatch and valsMatch
    }

    def mergesOverlappingKeys: SpecsResult = {
      val map1 = Map("a" -> List("b", "c"))
      val map2 = Map("a" -> List("c", "d"))
      val merged = HttpUtil.mergeParameters(map1, map2)
      val keysMatch = merged.keys must containTheSameElementsAs(uniqueKeys(map1, map2).toSeq)
      val valsMatch = allValues(merged) must containTheSameElementsAs(allValues(map1) ++ allValues(map2))
      keysMatch and valsMatch
    }

    private def mergesEmptyMap(m1: Map[String, List[String]], m2: Map[String, List[String]]): SpecsResult = {
      val merged = HttpUtil.mergeParameters(m1, m2)
      val keysMatch = merged.keys must containTheSameElementsAs(uniqueKeys(m1, m2).toSeq)
      val valsMatch = allValues(merged) must containTheSameElementsAs(allValues(m1) ++ allValues(m2))
      keysMatch and valsMatch
    }

    def mergesEmptyMap: SpecsResult = {
      val empty = Map[String, List[String]]()
      val nonEmpty = Map("a" -> List("c", "d"))
      mergesEmptyMap(empty, nonEmpty) and mergesEmptyMap(nonEmpty, empty)
    }
  }
}
