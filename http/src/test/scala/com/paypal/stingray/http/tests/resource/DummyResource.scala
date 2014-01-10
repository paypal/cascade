package com.paypal.stingray.http.tests.resource

import scalaz._
import Scalaz._
import spray.http._
import StatusCodes._
import com.paypal.stingray.common.logging.LoggingSugar
import spray.http.HttpResponse
import scala.concurrent._
import com.paypal.stingray.http.resource._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 4/4/13
 * Time: 5:08 PM
 */

class DummyResource extends SimpleResource[HttpRequest, Unit, Map[String, String], NoBody] with LoggingSugar with NoParsing {

  override protected lazy val logger = getLogger[DummyResource]

  override def isAuthorized(p: HttpRequest): Future[Option[Unit]] = {
    p.headers.find(_.lowercaseName == "unauthorized").isEmpty.option(()).continue
  }

  override lazy val responseContentType: ContentType = ContentTypes.`text/plain`

  override lazy val acceptableContentTypes: List[ContentType] = List(ContentTypes.`application/json`, ContentTypes.`text/plain`)

  override def doGet(r: HttpRequest): Future[HttpResponse] = for {
    query <- r.uri.query.continue
    param <- query.get("foo").orHaltWith(BadRequest, "no query param")
    _ <- ("bar" === param).orHaltWith(BadRequest, "wrong query param")
    (foo, bar) <- ("foo", "bar").some.orHaltWith(BadRequest, "what")
    accept <- r.header[HttpHeaders.Accept].orHaltWith(BadRequest, "no accept header")
    _ <- ((accept.mediaRanges.size === 1) && (accept.mediaRanges(0).value == MediaTypes.`text/plain`.value)).orHaltWith(BadRequest, "no accept header")
  } yield HttpResponse(OK, "pong")

  override def doPostAsCreate(r: HttpRequest, auth: Unit, body: Map[String, String]): Future[(HttpResponse, Option[String])] = for {
    param <- body.get("foo").orHaltWith(BadRequest, "wrong json in body")
    _ <- ("bar" === param).orHaltWith(BadRequest, "wrong json in body")
  } yield (HttpResponse(Created, "pong"), "foobar".some)

  override def doPut(r: HttpRequest, body: Option[String]): Future[HttpResponse] = for {
    _ <- body.isEmpty.orHaltWith(BadRequest, "somehow got a body")
  } yield HttpResponse(OK, "pong")

}
