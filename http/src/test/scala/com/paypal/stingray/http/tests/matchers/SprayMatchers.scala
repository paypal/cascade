package com.paypal.stingray.http.tests.matchers

import org.specs2.matcher.{Matcher, Expectable, MatchResult}
import spray.http._
import spray.http.HttpEntity._
import com.paypal.stingray.common.option._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try
import akka.actor.{Props, ActorRef, ActorSystem}
import com.paypal.stingray.http.tests.resource.executeResourceDriver
import com.paypal.stingray.http.resource.AbstractResourceActor

/**
 * Utility match cases for testing [[com.paypal.stingray.http.resource.AbstractResourceActor]]
 * and [[com.paypal.stingray.http.resource.ResourceServiceComponent.ResourceService]] implementations
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
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeGivenData[ParsedRequest](req: HttpRequest,
                                           requestParser: HttpRequest => Try[ParsedRequest],
                                           code: StatusCode) =
    new ResponseHasCode[ParsedRequest](req, requestParser, code)

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyGivenData[ParsedRequest](req: HttpRequest,
                                           requestParser: HttpRequest => Try[ParsedRequest],
                                           body: HttpEntity) =
    new ResponseHasBody[ParsedRequest](req, requestParser, body)

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyStringGivenData[ParsedRequest](req: HttpRequest,
                                                 requestParser: HttpRequest => Try[ParsedRequest],
                                                 body: String) =
    new ResponseHasBodyString[ParsedRequest](req, requestParser, body)

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyLike[ParsedRequest](req: HttpRequest,
                                      requestParser: HttpRequest => Try[ParsedRequest])
                                     (f: HttpEntity => MatchResult[Any]) =
    new ResponseHasBodyLike[ParsedRequest](req, requestParser, f)

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyStringLike[ParsedRequest](req: HttpRequest,
                                            requestParser: HttpRequest => Try[ParsedRequest])
                                           (f: String => MatchResult[Any]) =
    new ResponseHasBodyStringLike[ParsedRequest](req, requestParser, f)

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeAndBodyLike[ParsedRequest](req: HttpRequest,
                                             requestParser: HttpRequest => Try[ParsedRequest],
                                             code: StatusCode)
                                            (f: HttpEntity => MatchResult[Any]) =
    new ResponseHasCodeAndBodyLike[ParsedRequest](req, requestParser, code, f)

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeAndBodyStringLike[ParsedRequest](req: HttpRequest,
                                                   requestParser: HttpRequest => Try[ParsedRequest],
                                                   code: StatusCode)
                                                  (f: String => MatchResult[Any]) =
    new ResponseHasCodeAndBodyStringLike[ParsedRequest](req, requestParser, code, f)

  /**
   * Requires that a run request must have a given header in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInResponseWithHeaderContaining[ParsedRequest](req: HttpRequest,
                                                          requestParser: HttpRequest => Try[ParsedRequest],
                                                          header: HttpHeader) =
    new ResponseHasHeaderContainingValue[ParsedRequest](req, requestParser, header)

  /**
   * Requires that a run request must have a given header and header value in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInResponseWithNonEmptyHeader[ParsedRequest](req: HttpRequest,
                                                        requestParser: HttpRequest => Try[ParsedRequest],
                                                        header: String) =
    new ResponseHasNonEmptyHeader[ParsedRequest](req, requestParser, header)

  /**
   * Requires that a run request must have a given `Content-Type` header in its response
   * @param req the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param cType the content type
   * @tparam ParsedRequest the parsed request type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInContentType[ParsedRequest](req: HttpRequest,
                                         requestParser: HttpRequest => Try[ParsedRequest],
                                         cType: ContentType) =
    new ResponseHasContentType[ParsedRequest](req, requestParser, cType)

  /**
   * Requires that a run request must have a certain response code
   * @param req the request to run the request to run
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasCode[ParsedRequest](req: HttpRequest,
                                       requestParser: HttpRequest => Try[ParsedRequest],
                                       code: StatusCode)
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasBody[ParsedRequest](req: HttpRequest,
                                       requestParser: HttpRequest => Try[ParsedRequest],
                                       body: HttpEntity)
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasBodyString[ParsedRequest](req: HttpRequest,
                                             requestParser: HttpRequest => Try[ParsedRequest],
                                             body: String)
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasCodeAndBodyLike[ParsedRequest](req: HttpRequest,
                                                  requestParser: HttpRequest => Try[ParsedRequest],
                                                  code: StatusCode,
                                                  f: HttpEntity => MatchResult[Any])
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasCodeAndBodyStringLike[ParsedRequest](req: HttpRequest,
                                                        requestParser: HttpRequest => Try[ParsedRequest],
                                                        code: StatusCode,
                                                        f: String => MatchResult[Any])
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasBodyLike[ParsedRequest](req: HttpRequest,
                                           requestParser: HttpRequest => Try[ParsedRequest],
                                           f: HttpEntity => MatchResult[Any])
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasBodyStringLike[ParsedRequest](req: HttpRequest,
                                                 requestParser: HttpRequest => Try[ParsedRequest],
                                                 f: String => MatchResult[Any])
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasHeaderContainingValue[ParsedRequest](req: HttpRequest,
                                                        requestParser: HttpRequest => Try[ParsedRequest],
                                                        header: HttpHeader)
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasNonEmptyHeader[ParsedRequest](req: HttpRequest,
                                                 requestParser: HttpRequest => Try[ParsedRequest],
                                                 header: String)
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
   * @tparam ParsedRequest the parsed request type
   */
  class ResponseHasContentType[ParsedRequest](req: HttpRequest,
                                              requestParser: HttpRequest => Try[ParsedRequest],
                                              cType: ContentType)
    extends Matcher[ActorRef => AbstractResourceActor] {

    override def apply[S <: ActorRef => AbstractResourceActor](r: Expectable[S]): MatchResult[S] = {
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
