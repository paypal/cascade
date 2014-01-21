package com.paypal.stingray.concurrent.tests

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Try

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.concurrent.tests.future
 *
 * User: aaron
 * Date: 8/1/13
 * Time: 2:51 PM
 */
package object future {
  implicit class RichFuture[T](fut: Future[T]) {
    def block(dur: Duration = 1.second): Try[T] = {
      Try(Await.result(fut, dur))
    }
    def blockUnsafe(dur: Duration = 1.second): T = {
      Await.result(fut, dur)
    }
  }
}
