package com.paypal.stingray

import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.Logger
import com.paypal.stingray.concurrent.future._

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.concurrent
 *
 * User: aaron
 * Date: 7/3/13
 * Time: 11:43 AM
 */
package object concurrent {

  implicit val promiseMonad: Monad[Promise] = new Monad[Promise] {
    def point[A](a: => A): Promise[A] = {
      Promise.apply(a)(Strategy.Sequential)
    }

    def bind[A, B](fa: Promise[A])(f: (A) => Promise[B]): Promise[B] = {
      fa.flatMap { a =>
        f(a)
      }
    }
  }

  def futureMonad(implicit context: ExecutionContext): Monad[Future] = new Monad[Future] {
    def point[A](a: => A): Future[A] = {
      try {
        Future.successful(a)
      } catch {
        case t: Throwable => Future.failed(t)
      }
    }

    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = {
      fa.flatMap(f)
    }
  }
}
