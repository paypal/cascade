package com.paypal.stingray.http.tests.resource

import spray.http._
import StatusCodes._
import spray.http.HttpResponse
import scala.concurrent._
import com.paypal.stingray.http.resource._
import spray.http.HttpHeaders.RawHeader
import akka.actor.Actor
import com.paypal.stingray.http.tests.resource.DummyResource.{SleepRequest, PostRequest, LanguageRequest, GetRequest}
import com.paypal.stingray.http.resource.HttpResourceActor.{ResourceContext, ProcessRequest}

/**
 * Dummy implementation of a Spray resource. Does not perform additional parsing of requests, expects a basic type
 * for POST body, and nothing for PUT body.
 *
 * Useful when the action of a request through an HTTP server is important to model, as opposed to mocking a resource.
 */
class DummyResource(requestContext: ResourceContext)
  extends AbstractResourceActor(requestContext) {

  /**
   * the synchronous context this resource uses to construct futures in its methods
   */
  implicit val executionContext: ExecutionContext = new ExecutionContext {
    override def reportFailure(t: Throwable) {
      log.warning(t.getMessage, t)
    }
    override def execute(runnable: Runnable) {
      runnable.run()
    }
  }

  /** Default response content type is `text/plain` */
  override val responseContentType: ContentType = ContentTypes.`text/plain`

  /** Default accepted content types are `application/json` and `text/plain` */
  override val acceptableContentTypes: List[ContentType] =
    List(ContentTypes.`application/json`, ContentTypes.`text/plain`)

  /**
   * A dummy GET request must have a query param "foo=bar" and an Accept header with a single value `text/plain`
   * @param req the request
   */
  def doGet(req: GetRequest): Unit = {
    if (req.foo == "bar")
      completeToJSON(OK, "pong")
    else
      errorCode(BadRequest, "incorrect parameters")
  }

  /**
   * Sleep for a specified number of ms
   * @param req request object
   */
  def doSleep(req: SleepRequest): Unit = {
    Thread.sleep(req.millis)
    complete(HttpResponse(OK, "pong"))
  }

  def setContentLanguage(req: LanguageRequest): Unit = {
    complete(HttpResponse(OK, "Gutentag!", List(RawHeader("Content-Language", "de"))))
  }

  /**
   * A dummy POST request must have a body "{"foo":"bar"}"
   * @param req the request
   * @return the response for the post and the new location
   */
  def doPostAsCreate(req: PostRequest): Unit = {
    if (req.foo == "bar")
      completeToJSON(Created, "pong", "foobar")
    else
      errorCode(BadRequest, "incorrect parameters")
  }

  /**
   * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
   * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
   * safe to store mutable state during this receive function.
   * When the request is finished, [[complete]] must be called
   *
   * @return The receive function to be applied when a parsed request object or other actor message is received
   */
  override protected def resourceReceive: Actor.Receive = {
    case ProcessRequest(req: GetRequest) => doGet(req)
    case ProcessRequest(req: LanguageRequest) => setContentLanguage(req)
    case ProcessRequest(req: PostRequest) => doPostAsCreate(req)
    case ProcessRequest(req: SleepRequest) => doSleep(req)
  }
}

object DummyResource {
  case class GetRequest(foo: String)
  case class SleepRequest(millis: Long)
  case class LanguageRequest()
  case class PostRequest(foo: String)
}
