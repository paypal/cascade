package com.paypal.stingray.http.tests.matchers

import org.specs2.matcher.{Matcher, Expectable, MatchResult}
import spray.http._
import spray.http.HttpEntity._
import com.paypal.stingray.http.resource.{Resource, ResourceDriver}
import com.paypal.stingray.common.option._
import scala.concurrent._
import scala.concurrent.duration._
import language.postfixOps
import scala.util.Try

trait SprayMatchers {

  lazy val driver: ResourceDriver = new ResourceDriver{}

  def resultInCodeGivenData[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                        pathParts: Map[String, String],
                                                                        code: StatusCode) =
    new ResponseHasCode[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, code)

  def resultInBodyGivenData[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                        pathParts: Map[String, String],
                                                                        body: HttpEntity) =
    new ResponseHasBody[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, body)

  def resultInBodyStringGivenData[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                              pathParts: Map[String, String],
                                                                              body: String) =
    new ResponseHasBodyString[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, body)

  def resultInBodyLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                   pathParts: Map[String, String])
                                                                  (f: HttpEntity => MatchResult[Any]) =
    new ResponseHasBodyLike[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, f)

  def resultInBodyStringLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                         pathParts: Map[String, String])
                                                                        (f: String => MatchResult[Any]) =
    new ResponseHasBodyStringLike[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, f)

  def resultInCodeAndBodyLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                          pathParts: Map[String, String],
                                                                          code: StatusCode)
                                                                         (f: HttpEntity => MatchResult[Any]) =
    new ResponseHasCodeAndBodyLike[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, code, f)

  def resultInCodeAndBodyStringLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                                pathParts: Map[String, String],
                                                                                code: StatusCode)
                                                                               (f: String => MatchResult[Any]) =
    new ResponseHasCodeAndBodyStringLike[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, code, f)

  def resultInResponseWithHeaderContaining[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                                       pathParts: Map[String, String],
                                                                                       header: HttpHeader) =
    new ResponseHasHeaderContainingValue[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, header)

  def resultInResponseWithNonEmptyHeader[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                                     pathParts: Map[String, String],
                                                                                     header: String) =
    new ResponseHasNonEmptyHeader[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, header)

  def resultInContentType[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                      pathParts: Map[String, String],
                                                                      expected: ContentType) =
    new ResponseHasContentType[ParsedRequest, AuthInfo, PostBody, PutBody](req, pathParts, expected)

  class ResponseHasCode[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                    pathParts: Map[String, String],
                                                                    code: StatusCode)
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
      result(
        code == resp.status,
        s"Response has code: ${code.intValue}",
        s"Response has code: ${resp.status.intValue} (body: ${Try(resp.entity.asString).getOrElse("not available")}) expected: $code",
        r
      )
    }
  }

  class ResponseHasBody[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                    pathParts: Map[String, String],
                                                                    body: HttpEntity)
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
      result(
        body == resp.entity,
        "Expected response body found",
        s"response body: ${resp.entity.asString} is not equal to $body",
        r
      )
    }
  }

  class ResponseHasBodyString[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                          pathParts: Map[String, String],
                                                                          string: String)
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
      result(
        string == resp.entity.asString,
        "Expected response body found",
        s"response body: ${resp.entity.asString} is not equal to $string",
        r
      )
    }
  }

  class ResponseHasCodeAndBodyLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                               pathParts: Map[String, String],
                                                                               code: StatusCode,
                                                                               f: HttpEntity => MatchResult[Any])
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
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

  class ResponseHasCodeAndBodyStringLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                                     pathParts: Map[String, String],
                                                                                     code: StatusCode,
                                                                                     f: String => MatchResult[Any])
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
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

  class ResponseHasBodyLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                        pathParts: Map[String, String],
                                                                        f: HttpEntity => MatchResult[Any])
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
      val matchResult = f(resp.entity)
      result(
        matchResult.isSuccess,
        "success",
        matchResult.message,
        r
      )
    }
  }

  class ResponseHasBodyStringLike[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                              pathParts: Map[String, String],
                                                                              f: String => MatchResult[Any])
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
      val matchResult = f(resp.entity.asString)
      result(
        matchResult.isSuccess,
        "success",
        matchResult.message,
        r
      )
    }
  }

  class ResponseHasHeaderContainingValue[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                                     pathParts: Map[String, String],
                                                                                     header: HttpHeader)
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
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

  class ResponseHasNonEmptyHeader[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                              pathParts: Map[String, String],
                                                                              headerName: String)
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
      val hdr = resp.headers.find(_.lowercaseName == headerName)
      result(
        hdr.exists(_.value.trim != ""),
        s"header $headerName exists and is non-empty",
        hdr.map(_ => s"header $headerName exists but is empty")
          .getOrElse(s"header $headerName does not exist"),
        r
      )
    }
  }

  class ResponseHasContentType[ParsedRequest, AuthInfo, PostBody, PutBody](req: HttpRequest,
                                                                           pathParts: Map[String, String],
                                                                           cType: ContentType)
    extends Matcher[Resource[ParsedRequest, AuthInfo, PostBody, PutBody]] {

    override def apply[S <: Resource[ParsedRequest, AuthInfo, PostBody, PutBody]](r: Expectable[S]): MatchResult[S] = {
      val resp = Await.result(driver.serveSync(req, r.value, pathParts), 2 seconds)
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
