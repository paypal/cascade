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
package com.paypal.cascade.http.tests.resource

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor.Actor
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.http.{HttpResponse, _}

import com.paypal.cascade.http.resource.HttpResourceActor.{ProcessRequest, ResourceContext}
import com.paypal.cascade.http.resource._
import com.paypal.cascade.http.tests.resource.DummyResource.{GetRequest, LanguageRequest, PostRequest, SleepRequest, _}

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

  /**
   * A dummy GET request must have a query param "foo=bar" and an Accept header with a single value `text/plain`
   * @param req the request
   */
  def doGet(req: GetRequest): Unit = {
    if (req.foo == "bar")
      completeToJSON(OK, "pong")
    else
      sendErrorResponse(BadRequest, "incorrect parameters")
  }

  /**
   * Send a sleep to a child actor for specified millis. Sleeper actor will sleep, then send FinishedSleeping()
   * @param req request object
   */
  def doSleep(req: SleepRequest): Unit = {
    context.system.scheduler.scheduleOnce(req.millis.milliseconds, self, FinishedSleeping())
  }

  def doSyncSleep(req: SyncSleep): Unit = {
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
      sendErrorResponse(BadRequest, "incorrect parameters")
  }

  /**
   * This method is overridden by the end-user to execute the requests served by this resource. The ParsedRequest object
   * will be sent to this message from ResourceActor via a tell. As an actor will be spun up for each request, it is
   * safe to store mutable state during this receive function. When the request is finished, complete must be called.
   *
   * @return The receive function to be applied when a parsed request object or other actor message is received
   */
  override protected def resourceReceive: Actor.Receive = {
    case ProcessRequest(req: GetRequest) => doGet(req)
    case ProcessRequest(req: LanguageRequest) => setContentLanguage(req)
    case ProcessRequest(req: PostRequest) => doPostAsCreate(req)
    case ProcessRequest(req: SleepRequest) => doSleep(req)
    case ProcessRequest(req: SyncSleep) => doSyncSleep(req)
    //extra actor messages to respond to
    case FinishedSleeping() => log.info("finished sleeping"); complete(HttpResponse(OK, "pong"))
  }
}

object DummyResource {
  sealed trait DummyRequest
  case class GetRequest(foo: String) extends DummyRequest
  case class SleepRequest(millis: Long) extends DummyRequest
  case class SyncSleep(millis: Long) extends DummyRequest
  case class FinishedSleeping() extends DummyRequest
  case class LanguageRequest() extends DummyRequest
  case class PostRequest(foo: String) extends DummyRequest
}
