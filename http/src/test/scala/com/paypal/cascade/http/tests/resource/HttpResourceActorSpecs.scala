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
package com.paypal.cascade.http.tests.resource

import java.util.concurrent.{CountDownLatch, TimeUnit}

import scala.concurrent.{ExecutionContext, Promise}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import spray.http._
import org.specs2.SpecificationLike
import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.ExecutionEnvironment

import com.paypal.cascade.akka.tests.actor.ActorSpecification
import com.paypal.cascade.common.tests.future._
import com.paypal.cascade.common.tests.util.CommonImmutableSpecificationContext
import com.paypal.cascade.http.resource.HttpResourceActor.ResourceContext
import com.paypal.cascade.http.resource.{AbstractResourceActor, HttpResourceActor}
import com.paypal.cascade.http.tests.actor.RefAndProbe
import com.paypal.cascade.http.tests.matchers.RefAndProbeMatchers
import com.paypal.cascade.http.tests.resource.DummyResource.{GetRequest, SleepRequest, SyncSleep}

class HttpResourceActorSpecs
  extends TestKit(ActorSystem("resource-actor-specs"))
  with SpecificationLike
  with ActorSpecification
  with ExecutionEnvironment { override def is(implicit ee: ExecutionEnv)= s2"""

  ResourceActor is the individual actor that executes an entire request against an AbstractResource. One is created per request.

    After the ResourceActor succeeds, it writes the appropriate HttpResponse to the return actor and stops                   ${Succeeds().writesToReturnActor}
    After the ResourceActor fails, it writes the appropriate failure HttpResponse to the return actor and stops              ${Fails().writesToReturnActor}
    After the ResourceActor succeeds, it writes the appropriate HttpResponse to the DummyRequestContext and stops            ${Succeeds().writesToRequestContext}
    After the ResourceActor fails, it writes the appropriate HttpResponse to the DummyRequestContext and stops               ${Fails().writesToRequestContext}

    The ResourceActor should be start-able from the reference.conf file                                                      ${Start().succeeds}

    The ResourceActor should time out if the request processor takes too long in async code                                  ${Start().timesOutOnAsyncRequestProcessor}
    The ResourceActor will still succeed if blocking code takes too long. DON'T BLOCK in HttpActors!                         ${Start().timeOutFailsOnBlockingRequestProcessor}

    The ResourceActor should accept application/json without a charset                                                       ${ContentTypeWithoutCharset().success}
    The ResourceActor should accept application/json with charset=utf-8                                                      ${ContentTypeWithCharset().success}
    The ResourceActor should reject application/json with other charsets                                                     ${ContentTypeWithWrongCharset().reject}
    The ResourceActor should reject other Content-Types                                                                      ${WrongContentType().reject}

    The actor calls the before() method                                                                                      ${BeforeAfter().beforeCalled}
    The actor calls the after() method                                                                                       ${BeforeAfter().afterCalled}
  """
  private val resourceGen = new DummyResource(_)

  implicit val executionContext: ExecutionContext = system.dispatcher

  sealed trait Context extends CommonImmutableSpecificationContext with RefAndProbeMatchers {

    protected lazy val reqParser: HttpResourceActor.RequestParser = _ => Success(GetRequest("bar"))

    protected lazy val req = HttpRequest()

    private val reqCtxHandlerPromise = Promise[HttpResponse]()
    protected lazy val (dummyReqCtx, reqCtxHandlerActor) = DummyRequestContext(req, reqCtxHandlerPromise)
    protected val reqCtxHandlerActorFuture = reqCtxHandlerPromise.future

    private val returnActorPromise = Promise[HttpResponse]()
    protected lazy val returnActorRefAndProbe = RefAndProbe(TestActorRef(new ResponseHandlerActor(returnActorPromise)))
    protected val returnActorFuture = returnActorPromise.future

    protected lazy val dummyResourceContext = ResourceContext(dummyReqCtx, reqParser,  Some(returnActorRefAndProbe.ref))

    protected lazy val resourceActorRefAndProbe: RefAndProbe[AbstractResourceActor] = RefAndProbe(TestActorRef(new DummyResource(dummyResourceContext)))

    override def before() {
      resourceActorRefAndProbe.ref ! HttpResourceActor.Start
    }
  }

  case class Start(implicit ee: ExecutionEnv) extends Context {

    def succeeds = {
      val props = HttpResourceActor.props(resourceGen, dummyReqCtx, reqParser, None)
      val started = Try(system.actorOf(props))
      started.map { a =>
          system.stop(a)
      }
      started must beASuccessfulTry
    }

    def timesOutOnAsyncRequestProcessor = {
      // this test results in a dead letter for the FinishedSleeping message if it passes. no big deal
      val veryShortTimeout = Duration(50, TimeUnit.MILLISECONDS)

      lazy val resourceActorCtor = new DummyResource(ResourceContext(
        reqContext = dummyReqCtx,
        reqParser = request => Success(SleepRequest(500)),
        mbReturnActor = None,
        resourceTimeout = veryShortTimeout
      ))
      val refAndProbe = RefAndProbe(TestActorRef(resourceActorCtor))
      refAndProbe.ref ! HttpResourceActor.Start
      reqCtxHandlerActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.ServiceUnavailable)
      }.await
    }

    def timeOutFailsOnBlockingRequestProcessor = {
      // this test results in a dead letter for the timeout as a natural consequence of it blocking
      val veryShortTimeout = Duration(50, TimeUnit.MILLISECONDS)

      lazy val resourceActorCtor = new DummyResource(ResourceContext(
        reqContext = dummyReqCtx,
        reqParser = request => Success(SyncSleep(500)),
        mbReturnActor = None,
        resourceTimeout = veryShortTimeout
      ))
      val refAndProbe = RefAndProbe(TestActorRef(resourceActorCtor))
      refAndProbe.ref ! HttpResourceActor.Start
      reqCtxHandlerActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await
    }
  }

  case class Succeeds(implicit ee: ExecutionEnv) extends Context {
    def writesToReturnActor = apply {
      val recvRes = returnActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await

      val stoppedRes = resourceActorRefAndProbe must beStopped

      recvRes and stoppedRes
    }

    def writesToRequestContext = apply {
      val recvRes = reqCtxHandlerActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await

      val stoppedRes = resourceActorRefAndProbe must beStopped

      recvRes and stoppedRes
    }
  }

  case class ContentTypeWithoutCharset(implicit ee: ExecutionEnv) extends Context {
    override lazy val req = HttpRequest(entity=HttpEntity(ContentType(MediaTypes.`application/json`), "hi"))

    def success = apply {
      val recvRes = returnActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await

      val stoppedRes = resourceActorRefAndProbe must beStopped

      recvRes and stoppedRes
    }
  }

  case class ContentTypeWithCharset(implicit ee: ExecutionEnv) extends Context {
    override lazy val req = HttpRequest(entity=HttpEntity(ContentTypes.`application/json`, "hi"))

    def success = apply {
      val recvRes = returnActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await

      val stoppedRes = resourceActorRefAndProbe must beStopped

      recvRes and stoppedRes
    }
  }

  case class ContentTypeWithWrongCharset(implicit ee: ExecutionEnv) extends Context {
    override lazy val req = HttpRequest(entity=HttpEntity(ContentType(MediaTypes.`application/json`, HttpCharsets.`UTF-16`), "hi"))

    def reject = apply {
      val recvRes = returnActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.UnsupportedMediaType)
      }.await

      val stoppedRes = resourceActorRefAndProbe must beStopped

      recvRes and stoppedRes
    }
  }

  case class WrongContentType(implicit ee: ExecutionEnv) extends Context {
    override lazy val req = HttpRequest(entity="hi")  // text/plain

    def reject = apply {
      val recvRes = returnActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.UnsupportedMediaType)
      }.await

      val stoppedRes = resourceActorRefAndProbe must beStopped

      recvRes and stoppedRes
    }
  }

  case class Fails(implicit ee: ExecutionEnv) extends Context {
    private lazy val ex = new Exception("hello world")
    override protected lazy val reqParser: HttpResourceActor.RequestParser = { req: HttpRequest =>
      Failure(ex)
    }

    def writesToReturnActor = apply {
      val recvRes = returnActorFuture must beAnInstanceOf[HttpResponse].await
      val stoppedRes = resourceActorRefAndProbe must beStopped
      recvRes and stoppedRes
    }

    def writesToRequestContext = apply {
      val recvRes = reqCtxHandlerActorFuture.toTry must beASuccessfulTry.like {
        case HttpResponse(status, _, _, _) => status must beEqualTo(StatusCodes.BadRequest)
      }
      val stoppedRes = resourceActorRefAndProbe must beStopped
      recvRes and stoppedRes
    }
  }

  case class BeforeAfter() extends Context {

    val beforeLatch = new CountDownLatch(1)
    val afterLatch = new CountDownLatch(1)

    class WithBeforeAfter(ctx: ResourceContext) extends DummyResource(ctx) {
      override def before(method: HttpMethod): Unit = {
        beforeLatch.countDown()
      }
      override def after(resp: HttpResponse): Unit = {
        afterLatch.countDown()
      }
    }

    override protected lazy val resourceActorRefAndProbe: RefAndProbe[AbstractResourceActor] = RefAndProbe(TestActorRef(new WithBeforeAfter(dummyResourceContext)))

    def beforeCalled = apply {
      beforeLatch.await(500, TimeUnit.MILLISECONDS) must beTrue
    }
    def afterCalled = apply {
      afterLatch.await(500, TimeUnit.MILLISECONDS) must beTrue
    }
  }

}
