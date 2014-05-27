package com.paypal.stingray.http.tests.resource

import spray.http._
import StatusCodes._
import com.paypal.stingray.common.logging.LoggingSugar
import com.paypal.stingray.common.option._
import spray.http.HttpResponse
import scala.concurrent._
import com.paypal.stingray.http.resource._
import scala.util.{Success, Try}
import spray.http.HttpHeaders.RawHeader
import com.paypal.stingray.http.util.HttpUtil
import akka.actor.ActorRef

/**
 * Dummy implementation of a Spray resource. Does not perform additional parsing of requests, expects a basic type
 * for POST body, and nothing for PUT body.
 *
 * Useful when the action of a request through an HTTP server is important to model, as opposed to mocking a resource.
 */
class DummyResource(requestContext: ActorRef)
  extends AbstractResourceActor(requestContext)
  with LoggingSugar {

   def parseType[T](r: HttpRequest, data: String)(implicit m: Manifest[T]): Try[T] = {
    if (m == manifest[HttpRequest])
      Success(r.asInstanceOf[T])
    else if (m == manifest[Unit])
      Success(().asInstanceOf[T])
    else
      HttpUtil.parseType(r, data)(m)
  }

  /** This logger */
  override protected lazy val logger = getLogger[DummyResource]

  /**
   * the synchronous context this resource uses to construct futures in its methods
   */
  implicit val executionContext: ExecutionContext = new ExecutionContext {
    override def reportFailure(t: Throwable) {
      logger.warn(t.getMessage, t)
    }
    override def execute(runnable: Runnable) {
      runnable.run()
    }
  }


  /**
   * Authorized if a header marked `Unauthorized` is not sent.
   * @param r the parsed request
   * @return optionally, the AuthInfo for this request, or a Failure(halt)
   */
  override def isAuthorized(r: HttpRequest): Boolean = {
    if (r.headers.find(_.lowercaseName == "unauthorized").isEmpty) {
      true
    } else {
      false
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
  def doGet(r: HttpRequest): Future[(HttpResponse, Option[String])] = {
    for {
      query <- r.uri.query.continue
      param <- query.get("foo").orHaltWith(BadRequest, "no query param")
      _ <- ("bar" == param).orHaltWith(BadRequest, "wrong query param")
      (foo, bar) <- ("foo", "bar").some.orHaltWith(BadRequest, "what")
      accept <- r.header[HttpHeaders.Accept].orHaltWith(BadRequest, "no accept header")
      _ <- ((accept.mediaRanges.size == 1) && (accept.mediaRanges(0).value == MediaTypes.`text/plain`.value))
        .orHaltWith(BadRequest, "no accept header")
    } yield (HttpResponse(OK, "pong"), None)
  }

  def setContentLanguage(r: HttpRequest): Future[(HttpResponse, Option[String])] = {
    (HttpResponse(OK, "Gutentag!", List(RawHeader("Content-Language", "de"))), None).continue
  }

  /**
   * A dummy POST request must have a body "{"foo":"bar"}"
   * @param body the request
   * @return the response for the post and the new location
   */
  def doPostAsCreate(body: Map[String, String]): Future[(HttpResponse, Option[String])] = for {
    param <- body.get("foo").orHaltWith(BadRequest, "wrong json in body")
    _ <- ("bar" == param).orHaltWith(BadRequest, "wrong json in body")
  } yield (HttpResponse(Created, "pong"), "foobar".some)

  /**
   * A dummy PUT request must have no body
   * @param r the request
   * @return the response for the put
   */
  def doPut(r: Unit): Future[(HttpResponse, Option[String])] = (HttpResponse(OK, "pong"), None).continue

}
