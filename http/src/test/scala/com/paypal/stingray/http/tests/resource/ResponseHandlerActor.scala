package com.paypal.stingray.http.tests.resource

import scala.concurrent.Promise
import spray.http.HttpResponse
import akka.actor.{ActorSystem, Status, Actor}
import akka.testkit.TestActorRef
import com.paypal.stingray.common.actor.ServiceActor

/**
 * an actor that's designed to listen for responses that [[com.paypal.stingray.http.resource.ResourceActor]] completes.
 * when it receives a response, it will fulfill the promise that it's passed
 * @param respPromise the promise that it fulfills when it receives a message. fulfills it with success when it receives an [[HttpResponse]]
 *                    and failure when it receives a [[Throwable]] or a [[Status.Failure]]
 */
class ResponseHandlerActor(respPromise: Promise[HttpResponse]) extends ServiceActor {

  override def receive: Receive = {
    case t: Throwable =>
      log.error(s"failed with $t")
      respPromise.failure(t)

    case Status.Failure(t) =>
      log.error(s"failed with $t")
      respPromise.failure(t)

    case resp: HttpResponse =>
      log.debug(s"succeeded with $resp")
      respPromise.success(resp)
  }
}

object ResponseHandlerActor {
  /**
   * create a new [[ResponseHandlerActor]] for testing (using [[TestActorRef]])
   * @param actorSystem the actor system that implicitly gets passed to the [[TestActorRef]]
   * @return the new [[TestActorRef]] for the [[ResponseHandlerActor]] and the promise that the new actor will fulfill
   */
  def apply(implicit actorSystem: ActorSystem): (TestActorRef[ResponseHandlerActor], Promise[HttpResponse]) = {
    val promise = Promise[HttpResponse]()
    val ref = TestActorRef(new ResponseHandlerActor(promise))
    ref -> promise
  }
}
