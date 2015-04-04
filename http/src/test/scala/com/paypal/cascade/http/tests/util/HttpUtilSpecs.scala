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
package com.paypal.cascade.http.tests.util

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

import spray.http.{HttpEntity, HttpResponse}
import spray.http.StatusCodes._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.specs2.execute.{Result => SpecsResult}
import org.specs2.{ScalaCheck, Specification}

import com.paypal.cascade.common.logging.LoggingSugar
import com.paypal.cascade.http.url.StrPair
import com.paypal.cascade.http.util.HttpUtil

/**
 * Tests features of [[com.paypal.cascade.http.util.HttpUtil]]
 */
class HttpUtilSpecs extends Specification with ScalaCheck { override def is = s2"""

  HttpUtil contains a variety of different methods for doing common HTTP related tasks, such as parsing query strings

  HttpUtil#parseQueryStringToPairs should
    parse a well formed query string to valid pairs                                                                   ${ParseQueryStringToPairs().succeedsForWellFormedString}
    parse a well formed query string with url encoded values to valid pairs                                           ${ParseQueryStringToPairs().succeedsForURLEncodedString}
    parse a null string                                                                                               ${ParseQueryStringToPairs().succeedsForNullString}
    omit malformed query string key-value pairs                                                                       ${ParseQueryStringToPairs().omitsMalformedKVPs}

  HttpUtil#parseQueryStringToMap should
    parse a well formed query string to valid pairs                                                                   ${ParseQueryStringToMap().succeedsForWellFormedString}

  HttpUtil#mergeParameters should
    merge two valid maps together when keys don't overlap                                                             ${MergeParameters().mergesNonOverlappingKeys}
    merge two valid maps together when keys overlap                                                                   ${MergeParameters().mergesOverlappingKeys}
    merge an empty map with a non-empty one                                                                           ${MergeParameters().mergesEmptyMap}

  errorResponse
    properly returns a 500                                                                                            ${ErrorResponse().returns500}

  jsonOKResponse
    serializes json and returns OK status                                                                             ${JsonOK().returnsOK}

  toJsonBody
    Success returns proper http response                                                                              ${JsonBody().ok}
    Failure returns error in json format                                                                              ${JsonBody().error}
    converts case classes to json                                                                                     ${JsonBody().caseClass}

  """

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
      val decoded = (URLDecoder.decode(encoded._1, UTF_8.displayName), URLDecoder.decode(encoded._2, UTF_8.displayName))
      val qString = getQueryString(encoded :: list)
      HttpUtil.parseQueryStringToPairs(qString) must containTheSameElementsAs(decoded :: list)
    }

    def succeedsForNullString = {
      HttpUtil.parseQueryStringToPairs(null) must containTheSameElementsAs(List[StrPair]())
    }

    def omitsMalformedKVPs = forAll(genQueryPairs, genMalformedQueryStringPair) { (list, malformedStr) =>
      val qStringWithMalformed = s"${getQueryString(list)}&$malformedStr"
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

  case class ErrorResponse() extends Context {
    def returns500 = {
      val error = HttpUtil.errorResponse(new java.lang.Exception("Some Error"))
      (error must beAnInstanceOf[HttpResponse]) and (error.status must beEqualTo(InternalServerError))
    }
  }

  case class JsonOK() extends Context {
    def returnsOK = {
      val expected = """{"key":"value"}"""
      val body = Map("key" -> "value")
      val resp = HttpUtil.jsonOKResponse(body)
      ((resp must beAnInstanceOf[HttpResponse]) and (resp.status must beEqualTo(OK))) and (resp.entity.data.asString must beEqualTo(expected))
    }
  }

  case class JsonBody() extends Context {
    def ok = {
      val body = Map("key" -> "value")
      val expected = """{"key":"value"}"""
      val resp = HttpUtil.toJsonBody(body)
      (resp must beAnInstanceOf[HttpEntity]) and (resp.data.asString must beEqualTo(expected))
    }
    def error = {
      class Foo
      val body = new Foo
      val resp = HttpUtil.toJsonBody(body)
      (resp must beAnInstanceOf[HttpEntity]) and (resp.data.asString must startWith("Error serializing json body: "))
    }
    def caseClass = {
      case class Something(a: Int, b: String)
      val newSomething = Something(56, "Hello")
      val expected = """{"a":56,"b":"Hello"}"""
      val resp = HttpUtil.toJsonBody(newSomething)
      (resp must beAnInstanceOf[HttpEntity]) and (resp.data.asString must beEqualTo(expected))
    }
  }

}
