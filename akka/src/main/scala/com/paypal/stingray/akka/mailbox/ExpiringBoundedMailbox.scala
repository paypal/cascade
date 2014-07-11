package com.paypal.stingray.akka.mailbox

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{Duration, FiniteDuration}

import ExpiringBoundedMailbox._
import akka.actor._
import akka.dispatch._
import com.paypal.stingray.akka.actor.UnhandledMessageException
import com.paypal.stingray.akka.config._
import com.typesafe.config.Config

/**
 * akka BoundedMailbox that will immediately send a Status.Failure to the sender
 * if the mailbox is full and the message can't be delivered within the push timeout,
 * or if the message sits in the mailbox longer than the expiration timeout.
 *
 * Note that this Mailbox requires an actor system on creation.
 */
case class ExpiringBoundedMailbox(capacity: Int, pushTimeOut: FiniteDuration, messageExpiration: FiniteDuration)
  extends MailboxType with ProducesMessageQueue[BoundedMailbox.MessageQueue] {

  def this(settings: ActorSystem.Settings, config: Config) = {
    this(config.getOptionalInt(capacityStr).orThrowConfigError(capacityStr),
      Duration(config.getOptionalDuration(pushTimeoutStr, TimeUnit.MILLISECONDS).orThrowConfigError(pushTimeoutStr), TimeUnit.MILLISECONDS),
      Duration(config.getOptionalDuration(messageExpirationStr, TimeUnit.MILLISECONDS).orThrowConfigError(messageExpirationStr), TimeUnit.MILLISECONDS)
    )
  }

  if (capacity < 0) throw new IllegalArgumentException("The capacity for BoundedMailbox can not be negative")
  if (pushTimeOut.eq(null)) throw new IllegalArgumentException("The push time-out for BoundedMailbox can not be null") // scalastyle:ignore null

  override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue =
    new BoundedMailbox.MessageQueue(capacity, pushTimeOut) {

      private case class TimedEnvelope(envelope: Envelope, timestamp: Long, receiver: ActorRef)
      val exprMillis = messageExpiration.toMillis

      override def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
        val env = Envelope(TimedEnvelope(handle, System.currentTimeMillis(), receiver), handle.sender, system.get)
        if (pushTimeOut.length >= 0) {
          if (!queue.offer(env, pushTimeOut.length, pushTimeOut.unit) && (env.sender ne Actor.noSender)) {
            //respond immediately to sender with a Status.Failure
            handle.sender ! Status.Failure(
              new UnhandledMessageException(s"Mailbox full or timed out. Failed message: ${handle.message.getClass}"))
          }
        } else {
          queue.put(env)
        }
      }

      override def dequeue(): Envelope = {
        val d = super.dequeue()
        d match {
          case Envelope(TimedEnvelope(env, created, receiver), _) =>
            if (System.currentTimeMillis() - created >= exprMillis) {
              val ex = new UnhandledMessageException("message expired before being handled")
              if (env.sender.ne(Actor.noSender)) {
                //tell the sender immediately that the message failed
                env.sender ! Status.Failure(ex)
              }
              //return ExpiredLetter rather than original Envelope
              Envelope(ExpiredLetter(env.message, env.sender, receiver), env.sender, system.get)
            } else {
              env
            }
          //akka may send null messages to mailboxes on startup. this is not an error condition and is expected behavior.
          case null => null //scalastyle:ignore null
          case Envelope(msg, _) => throw new UnhandledMessageException(s"Only TimedEnvelopes should be in this mailbox. Found ${msg.getClass}")
        }
      }
    }

}

/**
 * A wrapper for Messages that went "stale" by sitting in the Mailbox's queue for longer than
 * ``mailbox-expiration-time``ms before being dequeued. [[ExpiredLetter]]s are still delivered
 * to the original receiving Actor, as this is how akka works. In the future, it may make sense
 * to have an [[ExpiredLetter]] event stream similar to the [[DeadLetter]] stream/handler, and avoid
 * delivering [[ExpiredLetter]]s to the original recipient.
 *
 * @param message the original message sent by sender
 * @param sender the sender of the expired message
 * @param recipient the intended recipient of the expired message
 */
case class ExpiredLetter(message: Any, sender: ActorRef, recipient: ActorRef)

object ExpiringBoundedMailbox {
  private[ExpiringBoundedMailbox] val capacityStr = "mailbox-capacity"
  private[ExpiringBoundedMailbox] val pushTimeoutStr = "mailbox-push-timeout-time"
  private[ExpiringBoundedMailbox] val messageExpirationStr = "mailbox-expiration-time"
}
