package com.paypal.stingray.common

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.either
 *
 * User: aaron
 * Date: 7/17/13
 * Time: 3:59 PM
 */
package object either {
  def eitherT[Fail, Succ](fut: => Future[Succ])(implicit ctx: ExecutionContext): EitherT[Future, Fail, Succ] = {
    EitherT.apply[Future, Fail, Succ] {
      fut.map(_.right[Fail])
    }
  }

  def eitherT[Fail, Succ](ethr: => Fail \/ Succ): EitherT[Future, Fail, Succ] = {
    EitherT.apply[Future, Fail, Succ] {
      Future.successful(ethr)
    }
  }

  final class EitherOps[A](self: A) {
    def toRight[X]: Either[X, A] = Right(self)

    def toLeft[X]: Either[A, X] = Left(self)
  }
  implicit def toEitherOps[A](a: => A) = new EitherOps(a)

}
