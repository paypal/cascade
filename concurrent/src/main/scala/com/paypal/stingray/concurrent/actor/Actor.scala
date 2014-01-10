package com.paypal.stingray.concurrent.actor

/**
 * com.stackmob.actors
 *
 * Copyright 2011 StackMob
 *
 * User: aaron
 * Date: 11/28/11
 * Time: 4:03 PM
 */

import scalaz._
import concurrent.{Promise, Strategy}
import java.util.concurrent.Executors
import com.paypal.stingray.common.validation.validating
import com.paypal.stingray.common.logging.LoggingSugar

/**
 * a fully typed actor that accepts messages and returns a result in a Promise[Validation[Throwable, Out]]
 * @tparam In the type of the messages
 * @tparam Out the type of the actor's response
 */
trait Actor[In, Out] extends LoggingSugar {
  import Actor._
  //making this anything but singleThreadExecutor means that we're not an actor anymore,
  //since messages could be processed concurrently and out of order
  implicit private val executorService = Executors.newSingleThreadExecutor
  implicit private val strategy = Strategy.Executor(executorService)

  /**
   * send a message to this actor
   * @param msg the message to send
   * @return a promise that will hold the result of computing the result of the message.
   *         the promise will contain a Failure if responder (below) threw an exception.
   *         otherwise it will contain a Success
   */
  def !(msg: In): Return[Out] = Promise(validating(responder(msg)))

  /**
   * alias for !
   */
  def send(msg: In): Return[Out] = this.!(msg)

  /**
   * the method to respond to incoming methods. override this with your own code
   * @param i the message sent to this actor
   * @return the method to process incoming messages. will be called once per message,
   *         in the global (across all threads) order that the messages are sent to this actor
   */
  protected def responder(i: In): Out
}

object Actor {
  type Return[Out] = Promise[Validation[Throwable, Out]]
  def apply[In, Out](f: In => Out): Actor[In, Out] = new Actor[In, Out] {
    override protected def responder(i: In) = f(i)
  }
}
