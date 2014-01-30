package com.paypal.stingray.http.tests.resource

import spray.http._
import StatusCodes._
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import spray.http.HttpResponse
import scala.concurrent._
import com.paypal.stingray.http.resource._
import scala.util.Try
import com.paypal.stingray.common.json._

/**
 * Dummy implementation of a Spray resource. Does not perform additional parsing of requests, expects a basic type
 * for POST body, and nothing for PUT body.
 *
 * Useful when the action of a request through an HTTP server is important to model, as opposed to mocking a resource.
 */
class DummyResource
  extends AbstractResource[HttpRequest, Unit, Map[String, String], NoBody]
  with LoggingSugar
  with NoParsing {

  /** This logger */
  override protected lazy val logger = getLogger[DummyResource]

  /**
   * Authorized if a header marked `Unauthorized` is not sent.
   * @param p the parsed request
   * @return optionally, the AuthInfo for this request, or a Failure(halt)
   */
  override def isAuthorized(p: HttpRequest): Future[Option[Unit]] = {
    if (p.headers.find(_.lowercaseName == "unauthorized").isEmpty) {
      Some(()).continue
    } else {
      halt(StatusCodes.Unauthorized)
    }
  }

  /** Default response content type is `text/plain` */
  override lazy val responseContentType: ContentType = ContentTypes.`text/plain`

  /** Default accepted content types are `application/json` and `text/plain` */
  override lazy val acceptableContentTypes: List[ContentType] =
    List(ContentTypes.`application/json`, ContentTypes.`text/plain`)

  /**
   * A dummy GET request must have a query param "foo=bar" and an Accept header with a single value `text/plain`
   * @param r the request
   * @return a response for the given request
   */
  override def doGet(r: HttpRequest): Future[HttpResponse] = for {
    query <- r.uri.query.continue
    param <- query.get("foo").orHaltWith(BadRequest, "no query param")
    _ <- ("bar" == param).orHaltWith(BadRequest, "wrong query param")
    (foo, bar) <- ("foo", "bar").some.orHaltWith(BadRequest, "what")
    accept <- r.header[HttpHeaders.Accept].orHaltWith(BadRequest, "no accept header")
    _ <- ((accept.mediaRanges.size == 1) && (accept.mediaRanges(0).value == MediaTypes.`text/plain`.value))
      .orHaltWith(BadRequest, "no accept header")
  } yield HttpResponse(OK, "pong")

  /**
   * A dummy POST request must have a body "{"foo":"bar"}"
   * @param r the request
   * @param auth the Auth object
   * @param body the body
   * @return the response for the post and the new location
   */
  override def doPostAsCreate(r: HttpRequest,
                              auth: Unit,
                              body: Map[String, String]): Future[(HttpResponse, Option[String])] = for {
    param <- body.get("foo").orHaltWith(BadRequest, "wrong json in body")
    _ <- ("bar" == param).orHaltWith(BadRequest, "wrong json in body")
  } yield (HttpResponse(Created, "pong"), "foobar".some)

  /**
   * A dummy PUT request must have no body
   * @param r the request
   * @param body the body
   * @return the response for the put
   */
  override def doPut(r: HttpRequest, body: Option[String]): Future[HttpResponse] = for {
    _ <- body.isEmpty.orHaltWith(BadRequest, "somehow got a body")
  } yield HttpResponse(OK, "pong")

  /**
   * Parse the POST body as JSON
   * @param r the HTTP request
   * @return optionally, an object of the PostBody type
   */
  override def parsePostBody(r: HttpRequest): Future[Option[Map[String, String]]] = for {
    parsed <- JsonUtil.fromJson[Map[String, String]](r.entity.asString).toOption.continue
  } yield parsed

  /**
   * Parse the PUT body as an Option
   * @param r the http request
   * @return optionally, an object of the PutBody type
   */
  override def parsePutBody(r: HttpRequest): Future[Option[NoBody]] = for {
    parsed <- Try { val a = r.entity.asString; if(a.isEmpty) None else Some(a) }.toOption.continue
  } yield parsed

}
