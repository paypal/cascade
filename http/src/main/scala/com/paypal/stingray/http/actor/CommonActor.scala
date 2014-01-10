package com.paypal.stingray.http.actor

import akka.actor.{ActorLogging, Actor, Status}

/**
 * Created with IntelliJ IDEA.
 * User: taylor
 * Date: 8/1/13
 * Time: 5:50 PM
 */

trait CommonActor extends Actor with ActorLogging {

  override def preStart() {
    log.info(s"Starting actor: ${self.path}")
    super.preStart()
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, s"Restarting actor: ${self.path}, message: ${message.getOrElse("")}")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable) {
    log.error(reason, s"Restarted actor: ${self.path}")
    super.postRestart(reason)
  }

  override def postStop() {
    log.info(s"Stopped actor: ${self.path}")
    super.postStop()
  }

}

trait ServiceActor extends CommonActor {
  override def unhandled(message: Any) {
    /**
     * 1) Publish an unhandled message to the actor system's even stream.
     * 2) Reply with a failure message to the sender.
     * 3) Throw an exception for delegation to the supervisor.
     */
    super.unhandled(message)
    val ex = new UnhandledMessageException(s"Unhandled message recieved by actor: ${self.path}, message: $message")
    sender ! Status.Failure(ex)
    throw ex
  }
}

class UnhandledMessageException(message: String) extends ActorException(message)

abstract class ActorException(message: String, cause: Option[Throwable] = None)
  extends Exception(message, cause.orNull)

