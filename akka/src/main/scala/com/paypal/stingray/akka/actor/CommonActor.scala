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
package com.paypal.cascade.akka.actor

import akka.actor.{UnhandledMessage, ActorLogging, Actor, Status}
import com.paypal.cascade.akka.mailbox.ExpiredLetter

/**
 * CommonActor is intended for top-level actors that have robust error-handling, and that are responsible for
 * receiving exceptions from other actors. Generally, there should only be one actor (or a small pool of actors)
 * extending this trait directly.
 */
trait CommonActor extends Actor with ActorLogging {

  /**
   * Can be overridden in subsequent actor implementations, but `super.postRestart(reason)` should also be called
   * to preserve consistent behavior
   * @param reason what triggered this restart cycle
   */
  override def postRestart(reason: Throwable): Unit = {
    log.debug(s"Restarted actor: ${self.path}")
    super.postRestart(reason)
  }
}

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
   *
   * [[com.paypal.cascade.akka.mailbox.ExpiredLetter]] messages from an [[com.paypal.cascade.akka.mailbox.ExpiringBoundedMailbox]]
   * are published to the system eventstream, similar to the dead letter stream, and are otherwise ignored.
   *
   * @param message The unhandled message
   * @throws UnhandledMessageException The unhandled message exception.
   */
  @throws[UnhandledMessageException]
  override def unhandled(message: Any): Unit = {
    message match {
      case em: ExpiredLetter => context.system.eventStream.publish(UnhandledMessage(message, sender(), self))
      case _ =>
        super.unhandled(message)
        val ex = new UnhandledMessageException(s"Unhandled message received by actor: ${self.path}, sender: ${sender()}, message: ${message.getClass}")
        sender ! Status.Failure(ex)
        throw ex
    }
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
