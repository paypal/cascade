package com.paypal.stingray.http.tests.resource

import scala.concurrent.Promise
import spray.http.HttpResponse
import akka.actor.{ActorSystem, Status, Actor}
import akka.testkit.TestActorRef

class ResponseHandlerActor(val respPromise: Promise[HttpResponse] = Promise.apply[HttpResponse]()) extends Actor {
  override def receive: Receive = {
    case t: Throwable => respPromise.failure(t)
    case Status.Failure(t) => respPromise.failure(t)
    case resp: HttpResponse => respPromise.success(resp)
  }
}

object ResponseHandlerActor {
  def apply(implicit actorSystem: ActorSystem): (TestActorRef[ResponseHandlerActor], Promise[HttpResponse]) = {
    val ref = TestActorRef[ResponseHandlerActor](new ResponseHandlerActor())
    ref -> ref.underlyingActor.respPromise
  }
}
