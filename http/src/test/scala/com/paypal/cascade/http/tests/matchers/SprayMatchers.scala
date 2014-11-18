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
package com.paypal.cascade.http.tests.matchers

import org.specs2.matcher.{Matcher, Expectable, MatchResult}
import spray.http._
import spray.http.HttpEntity._
import com.paypal.cascade.common.option._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try
import akka.actor.ActorSystem
import com.paypal.cascade.http.tests.resource.executeResourceDriver
import com.paypal.cascade.http.resource.{HttpResourceActor, AbstractResourceActor}
import com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext

/**
 * Utility match cases for testing [[com.paypal.cascade.http.resource.AbstractResourceActor]]
 * and [[com.paypal.cascade.http.resource.ResourceService]] implementations
 */
trait SprayMatchers {

  implicit val actorSystem: ActorSystem

  /** Default timeout for SprayMatcher responses. Default of 2 seconds; override if necessary. */
  lazy val sprayMatcherAwaitDuration: Duration = 2.seconds

  /**
   * Requires that a run request must have a certain response code
   * @param req the request to run the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeGivenData(req: HttpRequest,
                            requestParser: HttpResourceActor.RequestParser,
                            code: StatusCode): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasCode(req, requestParser, code)

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyGivenData(req: HttpRequest,
                            requestParser: HttpResourceActor.RequestParser,
                            body: HttpEntity): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasBody(req, requestParser, body)

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyStringGivenData(req: HttpRequest,
                                  requestParser: HttpResourceActor.RequestParser,
                                  body: String): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasBodyString(req, requestParser, body)

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyLike(req: HttpRequest,
                       requestParser: HttpResourceActor.RequestParser)
                       (f: HttpEntity => MatchResult[Any]): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasBodyLike(req, requestParser, f)

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyStringLike(req: HttpRequest,
                             requestParser: HttpResourceActor.RequestParser)
                             (f: String => MatchResult[Any]): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasBodyStringLike(req, requestParser, f)

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeAndBodyLike(req: HttpRequest,
                              requestParser: HttpResourceActor.RequestParser,
                              code: StatusCode)
                              (f: HttpEntity => MatchResult[Any]): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasCodeAndBodyLike(req, requestParser, code, f)

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeAndBodyStringLike(req: HttpRequest,
                                    requestParser: HttpResourceActor.RequestParser,
                                    code: StatusCode)
                                    (f: String => MatchResult[Any]): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasCodeAndBodyStringLike(req, requestParser, code, f)

  /**
   * Requires that a run request must have a given header in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInResponseWithHeaderContaining(req: HttpRequest,
                                           requestParser: HttpResourceActor.RequestParser,
                                           header: HttpHeader): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasHeaderContainingValue(req, requestParser, header)

  /**
   * Requires that a run request must have a given header and header value in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInResponseWithNonEmptyHeader(req: HttpRequest,
                                         requestParser: HttpResourceActor.RequestParser,
                                         header: String): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasNonEmptyHeader(req, requestParser, header)

  /**
   * Requires that a run request must have a given `Content-Type` header in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param cType the content type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInContentType(req: HttpRequest,
                          requestParser: HttpResourceActor.RequestParser,
                          cType: ContentType): Matcher[ResourceContext => AbstractResourceActor] =
    new ResponseHasContentType(req, requestParser, cType)

  /**
   * Requires that a run request must have a certain response code
   * @param req the request to run the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   */
  class ResponseHasCode(req: HttpRequest,
                        requestParser: HttpResourceActor.RequestParser,
                        code: StatusCode)
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      result(
        code == resp.status,
        s"Response has code: ${code.intValue}",
        s"Response has code: ${resp.status.intValue} (body: ${Try(resp.entity.asString).getOrElse("not available")}) expected: $code",
        r
      )
    }
  }

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   */
  class ResponseHasBody(req: HttpRequest,
                        requestParser: HttpResourceActor.RequestParser,
                        body: HttpEntity)
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      result(
        body == resp.entity,
        "Expected response body found",
        s"response body: ${resp.entity.asString} is not equal to $body",
        r
      )
    }
  }

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   */
  class ResponseHasBodyString(req: HttpRequest,
                              requestParser: HttpResourceActor.RequestParser,
                              body: String)
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      result(
        body == resp.entity.asString,
        "Expected response body found",
        s"response body: ${resp.entity.asString} is not equal to $body",
        r
      )
    }
  }

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   */
  class ResponseHasCodeAndBodyLike(req: HttpRequest,
                                   requestParser: HttpResourceActor.RequestParser,
                                   code: StatusCode,
                                   f: HttpEntity => MatchResult[Any])
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val matchResult = f(resp.entity)
      result(
        code == resp.status && matchResult.isSuccess,
        "success",
        if (code != resp.status)
          s"Response has code: ${resp.status.intValue} body: ${resp.entity.asString} expected: $code"
        else
          matchResult.message,
        r
      )
    }
  }

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   */
  class ResponseHasCodeAndBodyStringLike(req: HttpRequest,
                                         requestParser: HttpResourceActor.RequestParser,
                                         code: StatusCode,
                                         f: String => MatchResult[Any])
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val matchResult = f(resp.entity.asString)
      result(
        code == resp.status && matchResult.isSuccess,
        "success",
        if (code != resp.status)
          s"Response has code: ${resp.status.intValue} body: ${resp.entity.asString} expected: $code"
        else
          matchResult.message,
        r
      )
    }
  }

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   */
  class ResponseHasBodyLike(req: HttpRequest,
                            requestParser: HttpResourceActor.RequestParser,
                            f: HttpEntity => MatchResult[Any])
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val matchResult = f(resp.entity)
      result(
        matchResult.isSuccess,
        "success",
        matchResult.message,
        r
      )
    }
  }

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   */
  class ResponseHasBodyStringLike(req: HttpRequest,
                                  requestParser: HttpResourceActor.RequestParser,
                                  f: String => MatchResult[Any])
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val matchResult = f(resp.entity.asString)
      result(
        matchResult.isSuccess,
        "success",
        matchResult.message,
        r
      )
    }
  }

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   */
  class ResponseHasHeaderContainingValue(req: HttpRequest,
                                         requestParser: HttpResourceActor.RequestParser,
                                         header: HttpHeader)
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val hdr = resp.headers.find(_.lowercaseName == header.lowercaseName)
      result(
        hdr.exists(_ == header),
        s"header ${header.lowercaseName} has expected value: ${header.value}",
        hdr.map (v => s"header ${header.lowercaseName} exists but has value: $v expected: ${header.value}")
          .getOrElse(s"header ${header.lowercaseName} does not exist"),
        r
      )
    }
  }

  /**
   * Requires that a run request must have a given header and header value in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   */
  class ResponseHasNonEmptyHeader(req: HttpRequest,
                                  requestParser: HttpResourceActor.RequestParser,
                                  header: String)
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val hdr = resp.headers.find(_.lowercaseName == header)
      result(
        hdr.exists(_.value.trim != ""),
        s"header $header exists and is non-empty",
        hdr.map(_ => s"header $header exists but is empty")
          .getOrElse(s"header $header does not exist"),
        r
      )
    }
  }

  /**
   * Requires that a run request must have a given `Content-Type` header in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param cType the content type
   */
  class ResponseHasContentType(req: HttpRequest,
                               requestParser: HttpResourceActor.RequestParser,
                               cType: ContentType)
    extends Matcher[ResourceContext => AbstractResourceActor] {

    override def apply[S <: ResourceContext => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
      val respFuture = executeResourceDriver(req, r.value, requestParser)
      val resp = Await.result(respFuture, sprayMatcherAwaitDuration)
      val resultCType = resp.entity.some collect {
        case NonEmpty(c, _) => c
      }
      result(
        resultCType.exists(_ == cType),
        s"content type ${cType.toString()} found",
        resultCType.map(ct => s"content type is: $ct expected: $cType")
          .getOrElse("content type header not found"),
        r
      )
    }
  }
}
