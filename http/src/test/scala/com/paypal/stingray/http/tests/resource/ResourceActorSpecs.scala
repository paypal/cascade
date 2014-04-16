package com.paypal.stingray.http.tests.resource

import org.specs2.SpecificationLike
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.{Actor, ActorSystem}
import com.paypal.stingray.common.tests.actor.ActorSpecification
import com.paypal.stingray.http.resource.ResourceActor
import spray.http.{StatusCodes, HttpResponse, HttpRequest}
import scala.util.{Failure, Try, Success}
import scala.concurrent.{Promise, Future}
import com.paypal.stingray.common.tests.util.CommonImmutableSpecificationContext

class ResourceActorSpecs
  extends TestKit(ActorSystem("resource-actor-specs"))
  with SpecificationLike
  with ActorSpecification { override def is = s2"""

    ResourceActor is the individual actor that executes an entire request against an AbstractResource. One is created per request.

    After the ResourceActor succeeds, it writes the appropriate failure HttpResponse to the return actor and stops                     ${Succeeds().writesToReturnActor}
    After the ResourceActor fails, it writes the appropriate failure HttpResponse to the return actor and stops                        ${Fails().writesToReturnActor}
    After the ResourceActor succeeds, it writes the appropriate HttpResponse to the DummyRequestContext and stops                      ${Succeeds().writesToRequestContext}
    After the ResourceActor fails, it writes the appropriate HttpResponse to the DummyRequestContext and stops                         ${Fails().writesToRequestContext}

  """

  private val resource = new DummyResource

  case class RefAndProbe[T <: Actor](ref: TestActorRef[T], probe: TestProbe = TestProbe()) {
    probe.watch(ref)

    def hasStopped = {
      Try(probe.expectTerminated(ref)) must beSuccessfulTry
    }
  }

  sealed trait Context extends CommonImmutableSpecificationContext {

    protected lazy val reqParser: ResourceActor.RequestParser[Unit] = { req: HttpRequest =>
      Success(())
    }

    protected lazy val reqProcessor: ResourceActor.RequestProcessor[Unit] = { _: Unit =>
      Future.successful(HttpResponse() -> None)
    }

    protected lazy val req = HttpRequest()

    private val reqCtxHandlerPromise = Promise[HttpResponse]()
    protected lazy val (dummyReqCtx, reqCtxHandlerActor) = DummyRequestContext(req, reqCtxHandlerPromise)
    protected val reqCtxHandlerActorFuture = reqCtxHandlerPromise.future

    private val returnActorPromise = Promise[HttpResponse]()
    protected lazy val returnActorRefAndProbe = RefAndProbe(TestActorRef(new ResponseHandlerActor(returnActorPromise)))
    protected val returnActorFuture = returnActorPromise.future

    protected lazy val resourceActorRefAndProbe = RefAndProbe(TestActorRef(new ResourceActor(resource, dummyReqCtx, reqParser, reqProcessor, Some(returnActorRefAndProbe.ref))))

    override def before() {
      resourceActorRefAndProbe.ref ! ResourceActor.Start
    }
  }

  case class Succeeds() extends Context {
    def writesToReturnActor = apply {
      val recvRes = returnActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await

      val stoppedRes = resourceActorRefAndProbe.hasStopped

      recvRes and stoppedRes
    }

    def writesToRequestContext = apply {
      val recvRes = reqCtxHandlerActorFuture must beLike[HttpResponse] {
        case HttpResponse(statusCode, _, _, _) => statusCode must beEqualTo(StatusCodes.OK)
      }.await

      val stoppedRes = resourceActorRefAndProbe.hasStopped

      recvRes and stoppedRes
    }
  }

  case class Fails() extends Context {
    private lazy val ex = new Exception("hello world")
    override protected lazy val reqParser: ResourceActor.RequestParser[Unit] = { req: HttpRequest =>
      Failure(ex)
    }

    def writesToReturnActor = apply {
      val recvRes = returnActorFuture must beAnInstanceOf[HttpResponse].await
      val stoppedRes = resourceActorRefAndProbe.hasStopped
      recvRes and stoppedRes
    }

    def writesToRequestContext = apply {
      val recvRes = reqCtxHandlerActorFuture must beAnInstanceOf[HttpResponse].await
      val stoppedRes = resourceActorRefAndProbe.hasStopped
      recvRes and stoppedRes
    }

  }

}
