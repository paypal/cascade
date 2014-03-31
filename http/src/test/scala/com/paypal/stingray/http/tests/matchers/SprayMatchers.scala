package com.paypal.stingray.http.tests.matchers

import org.specs2.matcher.{Matcher, Expectable, MatchResult}
import spray.http._
import spray.http.HttpEntity._
import com.paypal.stingray.http.resource.{AbstractResource, ResourceDriver}
import com.paypal.stingray.common.option._
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try

/**
 * Utility match cases for testing [[com.paypal.stingray.http.resource.AbstractResource]]
 * and [[com.paypal.stingray.http.resource.ResourceServiceComponent.ResourceService]] implementations
 */
trait SprayMatchers {

  /** Default timeout for SprayMatcher responses. Default of 2 seconds; override if necessary. */
  lazy val sprayMatcherAwaitDuration: Duration = 2.seconds

  /**
   * Requires that a run request must have a certain response code
   * @param req the request to run the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeGivenData[ParsedRequest, AuthInfo](req: HttpRequest,
                                                     processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                     requestParser: HttpRequest => Try[ParsedRequest],
                                                     code: StatusCode) =
    new ResponseHasCode[ParsedRequest, AuthInfo](req, processFunction, requestParser, code)

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyGivenData[ParsedRequest, AuthInfo](req: HttpRequest,
                                                     processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                     requestParser: HttpRequest => Try[ParsedRequest],
                                                     body: HttpEntity) =
    new ResponseHasBody[ParsedRequest, AuthInfo](req, processFunction, requestParser, body)

  /**
   * Requires that a run request must have a certain response body
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyStringGivenData[ParsedRequest, AuthInfo](req: HttpRequest,
                                                           processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                           requestParser: HttpRequest => Try[ParsedRequest],
                                                           body: String) =
    new ResponseHasBodyString[ParsedRequest, AuthInfo](req, processFunction, requestParser, body)

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                requestParser: HttpRequest => Try[ParsedRequest])
                                               (f: HttpEntity => MatchResult[Any]) =
    new ResponseHasBodyLike[ParsedRequest, AuthInfo](req, processFunction, requestParser, f)

  /**
   * Requires that a run request must have a response body that passes a given comparison function
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInBodyStringLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                      processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                      requestParser: HttpRequest => Try[ParsedRequest])
                                                     (f: String => MatchResult[Any]) =
    new ResponseHasBodyStringLike[ParsedRequest, AuthInfo](req, processFunction, requestParser, f)

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeAndBodyLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                       processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                       requestParser: HttpRequest => Try[ParsedRequest],
                                                       code: StatusCode)
                                                      (f: HttpEntity => MatchResult[Any]) =
    new ResponseHasCodeAndBodyLike[ParsedRequest, AuthInfo](req, processFunction, requestParser, code, f)

  /**
   * Requires that a run request must have a certain response code and a response body that passes a given function
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInCodeAndBodyStringLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                             processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                             requestParser: HttpRequest => Try[ParsedRequest],
                                                             code: StatusCode)
                                                            (f: String => MatchResult[Any]) =
    new ResponseHasCodeAndBodyStringLike[ParsedRequest, AuthInfo](req, processFunction, requestParser, code, f)

  /**
   * Requires that a run request must have a given header in its response
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInResponseWithHeaderContaining[ParsedRequest, AuthInfo](req: HttpRequest,
                                                                    processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                                    requestParser: HttpRequest => Try[ParsedRequest],
                                                                    header: HttpHeader) =
    new ResponseHasHeaderContainingValue[ParsedRequest, AuthInfo](req, processFunction, requestParser, header)

  /**
   * Requires that a run request must have a given header and header value in its response
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInResponseWithNonEmptyHeader[ParsedRequest, AuthInfo](req: HttpRequest,
                                                                  processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                                  requestParser: HttpRequest => Try[ParsedRequest],
                                                                  header: String) =
    new ResponseHasNonEmptyHeader[ParsedRequest, AuthInfo](req, processFunction, requestParser, header)

  /**
   * Requires that a run request must have a given `Content-Type` header in its response
   * @param req the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param cType the content type
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   * @return an object that can yield a [[org.specs2.matcher.MatchResult]]
   */
  def resultInContentType[ParsedRequest, AuthInfo](req: HttpRequest,
                                                   processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                   requestParser: HttpRequest => Try[ParsedRequest],
                                                   cType: ContentType) =
    new ResponseHasContentType[ParsedRequest, AuthInfo](req, processFunction, requestParser, cType)

  /**
   * Requires that a run request must have a certain response code
   * @param req the request to run the request to run
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasCode[ParsedRequest, AuthInfo](req: HttpRequest,
                                                 processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                 requestParser: HttpRequest => Try[ParsedRequest],
                                                 code: StatusCode)
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasBody[ParsedRequest, AuthInfo](req: HttpRequest,
                                                 processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                 requestParser: HttpRequest => Try[ParsedRequest],
                                                 body: HttpEntity)
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param body the body required
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasBodyString[ParsedRequest, AuthInfo](req: HttpRequest,
                                                       processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                       requestParser: HttpRequest => Try[ParsedRequest],
                                                       body: String)
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasCodeAndBodyLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                            processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                            requestParser: HttpRequest => Try[ParsedRequest],
                                                            code: StatusCode,
                                                            f: HttpEntity => MatchResult[Any])
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param code the response code required
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasCodeAndBodyStringLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                                  processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                                  requestParser: HttpRequest => Try[ParsedRequest],
                                                                  code: StatusCode,
                                                                  f: String => MatchResult[Any])
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasBodyLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                     processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                     requestParser: HttpRequest => Try[ParsedRequest],
                                                     f: HttpEntity => MatchResult[Any])
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param f the comparison function
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasBodyStringLike[ParsedRequest, AuthInfo](req: HttpRequest,
                                                           processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                           requestParser: HttpRequest => Try[ParsedRequest],
                                                           f: String => MatchResult[Any])
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasHeaderContainingValue[ParsedRequest, AuthInfo](req: HttpRequest,
                                                                  processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                                  requestParser: HttpRequest => Try[ParsedRequest],
                                                                  header: HttpHeader)
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param header the header
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasNonEmptyHeader[ParsedRequest, AuthInfo](req: HttpRequest,
                                                           processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                           requestParser: HttpRequest => Try[ParsedRequest],
                                                           header: String)
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
   * @param processFunction the function which processes the request
   * @param requestParser function, which converts the request, into a parsed request
   * @param cType the content type
   * @tparam ParsedRequest the parsed request type
   * @tparam AuthInfo the auth container type
   */
  class ResponseHasContentType[ParsedRequest, AuthInfo](req: HttpRequest,
                                                        processFunction: ParsedRequest => Future[(HttpResponse, Option[String])],
                                                        requestParser: HttpRequest => Try[ParsedRequest],
                                                        cType: ContentType)
    extends Matcher[AbstractResource[AuthInfo]] {

    override def apply[S <: AbstractResource[AuthInfo]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(ResourceDriver.serveSync(req, r.value, processFunction, requestParser), sprayMatcherAwaitDuration)
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
