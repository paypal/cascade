package com.paypal.stingray.akka.actor

import akka.actor.{ActorLogging, Actor, Status}

/**
 * CommonActor is intended for top-level actors that have robust error-handling, and that are responsible for
 * receiving exceptions from other actors. Generally, there should only be one actor (or a small pool of actors)
 * extending this trait directly.
 */
trait CommonActor extends Actor with ActorLogging {}

/**
 * ServiceActor returns an error response for unhandled messages and escalates the error to the supervisor
 * by throwing an exception. For most child actors, this trait should be preferred.
 */
trait ServiceActor extends CommonActor {

  /**
   * Triggered when an actor receives a message that it doesn't recognize. In order:
   *
   * 1) Publishes an unhandled message to the actor system's event stream.
   * 2) Replies with a [[akka.actor.Status.Failure]] message to the sender.
   * 3) Throws an [[UnhandledMessageException]] for delegation to the supervisor.
   * @param message The unhandled message
   * @throws UnhandledMessageException The unhandled message exception.
   */
  @throws[UnhandledMessageException]
  override def unhandled(message: Any): Unit = {
    super.unhandled(message)
    val ex = new UnhandledMessageException(s"Unhandled message received by actor: ${self.path}, sender: ${sender()} message: $message")
    sender ! Status.Failure(ex)
    throw ex
  }
}

/**
 * Exception raised when an unhandled message is received by a CommonActor
 * @param message what happened
 */
class UnhandledMessageException(message: String) extends ActorException(message)

/**
 * Base type for custom exceptions raised within an actor
 * @param message what happened
 * @param cause if there was another exception that triggered this, it's here
 */
abstract class ActorException(message: String, cause: Option[Throwable] = None)
  extends Exception(message, cause.orNull)
