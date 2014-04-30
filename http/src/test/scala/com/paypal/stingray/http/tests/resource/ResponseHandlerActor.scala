package com.paypal.stingray.http.tests.resource

import scala.concurrent.Promise
import spray.http.HttpResponse
import akka.actor.{ActorSystem, Status}
import akka.testkit.TestActorRef
import com.paypal.stingray.akka.actor.ServiceActor

/**
 * an actor that's designed to listen for responses that [[com.paypal.stingray.http.resource.ResourceActor]] completes.
 * when it receives a response, it will fulfill the promise that it takes in.
 *
 * example usage:
 *
 * {{{
 *   //create the promise to be fulfilled and the appropriate handlers
 *   val prom = Promise[HttpResponse]()
 *   val respHandler = system.actorOf(new ResponseHandlerActor(prom)) //the actor that receives messages from ResourceActor
 *   val reqHandler = system.actorOf(new ResourceActor(resource, ctx, parser, processor, Some(respHandler))) //the actor that runs the AbstractResource
 *
 *   //start processing the request
 *   reqHandler ! ResourceHandler.Start
 *
 *   //wait for the response and verify it the right response code
 *   Await.result(prom.future, Duration.Inf).status must beEqualTo(StatusCodes.OK)
 * }}}
 * @param respPromise the promise that it fulfills when it receives a message. fulfills it with success when it receives an [[spray.http.HttpResponse]]
 *                    and failure when it receives a [[java.lang.Throwable]] or a [[akka.actor.Status.Failure]]
 */
class ResponseHandlerActor(val respPromise: Promise[HttpResponse]) extends ServiceActor {

  override def receive: Receive = {
    case t: Throwable =>
      log.error(s"failed with $t")
      respPromise.failure(t)
      context.stop(self)

    case Status.Failure(t) =>
      log.error(s"failed with $t")
      respPromise.failure(t)
      context.stop(self)

    case resp: HttpResponse =>
      log.debug(s"succeeded with $resp")
      respPromise.success(resp)
      context.stop(self)
  }
}

object ResponseHandlerActor {
  /**
   * create a new [[com.paypal.stingray.http.tests.resource.ResponseHandlerActor]] for testing (using [[akka.testkit.TestActorRef]])
   * @param actorSystem the actor system that implicitly gets passed to the [[akka.testkit.TestActorRef]]
   * @return the new [[akka.testkit.TestActorRef]] for the [[com.paypal.stingray.http.tests.resource.ResponseHandlerActor]] and the promise that the new actor will fulfill
   */
  def apply(implicit actorSystem: ActorSystem): TestActorRef[ResponseHandlerActor] = {
    val promise = Promise[HttpResponse]()
    TestActorRef(new ResponseHandlerActor(promise))
  }
}
