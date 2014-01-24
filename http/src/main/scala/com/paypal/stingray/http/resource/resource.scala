package com.paypal.stingray.http

import spray.http._
import spray.http.HttpEntity._
import StatusCodes._
import scala.concurrent._
import com.paypal.stingray.common.option._
import akka.actor.Status
import com.paypal.stingray.common.future._
import com.paypal.stingray.common.trys._
import scala.util.Try
import scala.concurrent.Future

/**
 * Utility methods for turning everyday datatypes into Futures that can possibly return (or throw) a
 * [[com.paypal.stingray.http.resource.HaltException]]. Methods of the form `orHalt` create a Future.
 * Methods of the form `orThrowHaltException` throw an Exception immediately and should only be used
 * when already inside a Future.
 */

package object resource {

  /** For resources that do not expect a body in requests */
  type NoBody = Option[String]

  /** For resources that do not perform any degree of authorization of incoming requests */
  type NoAuth = Unit


  def halt[T](status: StatusCode,
              entity: HttpEntity = Empty,
              headers: List[HttpHeader] = Nil) = none[T].orHaltWith(status, entity, headers)


  implicit class RichOptionHalt[A](v: Option[A]) {

    def orThrowHaltException(halt: => HttpResponse): A = {
      v.orThrow(new HaltException(halt))
    }

    def orThrowHaltException(status: => StatusCode,
                             entity: => HttpEntity = Empty,
                             headers: => List[HttpHeader] = Nil): A = {
      orThrowHaltException(HttpResponse(status, entity, headers))
    }

    def orHalt(halt: => HttpResponse): Future[A] = v match {
      case Some(a) => a.continue
      case None => Future.failed(new HaltException(halt))
    }

    def orHaltWith(status: => StatusCode,
                   entity: => HttpEntity = Empty,
                   headers: => List[HttpHeader] = Nil): Future[A] = {
      orHalt(HttpResponse(status, entity, headers))
    }

    def orError(entity: => HttpEntity = Empty,
                headers: => List[HttpHeader] = Nil): Future[A] = {
      orHaltWith(InternalServerError, entity, headers)
    }
  }

  implicit class RichEitherThrowableHalt[A](either: Either[Throwable, A]) {

    def orThrowHaltExceptionWithMessage(status: StatusCode)
                                       (f: String => String = identity): A = {
      either.fold(
        e => throw new HaltException(HttpResponse(status, f(e.getMessage))),
        a => a
      )
    }

    def orThrowHaltExceptionWithErrorMessage(f: String => String = identity): A =
      orThrowHaltExceptionWithMessage(InternalServerError)(f)

    def orHaltWithMessage(status: StatusCode)
                         (f: String => String = identity): Future[A] = either.fold(
      l => Future.failed(new HaltException(HttpResponse(status, f(l.getMessage)))),
      r => r.continue
    )

    def orErrorWithMessage(f: String => String = identity): Future[A] = orHaltWithMessage(InternalServerError)(f)

    // Useful for returning a failure case to the sender such that the sender receives a Future[T].
    // e.g.  sender ! someEither.orFailure
    def orFailure: Any = either.right.getOrElse(Status.Failure)

  }

  implicit class RichTryHalt[A](t: Try[A]) extends RichEitherThrowableHalt(t.toEither)

  implicit class RichEitherHalt[T, A](either: Either[T, A]) {
    def orThrowHaltException(haltFn: T => HttpResponse): A = {
      either.fold(
        t => throw new HaltException(haltFn(t)),
        a => a
      )
    }

    def orErrorNow(errorFn: T => HttpEntity = { _ => Empty }): A =
      orThrowHaltException(e => HttpResponse(InternalServerError, errorFn(e)))

    def orHalt(haltFn: T => HttpResponse): Future[A] = {
      either.fold(
        t => Future.failed[A](new HaltException(haltFn(t))),
        a => a.continue
      )
    }

    def orError(errorFn: T => HttpEntity = { _ => Empty }): Future[A] =
      orHalt(e => HttpResponse(InternalServerError, errorFn(e)))
  }

  implicit class RichBooleanHalt(v: Boolean) {

    def orThrowHaltException(halt: => HttpResponse) {
      if (v) ().continue else throw new HaltException(halt)
    }

    def orThrowHaltException(status: => StatusCode,
                             entity: => HttpEntity = Empty,
                             headers: => List[HttpHeader] = Nil) {
      orThrowHaltException(HttpResponse(status, entity, headers))
    }

    def orHalt(halt: => HttpResponse): Future[Unit] = {
      if (v) ().continue else Future.failed(new HaltException(halt))
    }

    def orHaltWith(status: => StatusCode,
                   entity: => HttpEntity = Empty,
                   headers: => List[HttpHeader] = Nil): Future[Unit] = {
      orHalt(HttpResponse(status, entity, headers))
    }

    def orError(entity: => HttpEntity = Empty,
                headers: => List[HttpHeader] = Nil): Future[Unit] = {
      orHaltWith(InternalServerError, entity, headers)
    }
  }

  implicit class RichFuture[T](v: Future[T]) {

    def orHalt(halt: PartialFunction[Throwable, HttpResponse])(implicit ec: ExecutionContext): Future[T] = {
      v.recoverWith {
        halt.andThen { resp => Future.failed(new HaltException(resp)) }
      }
    }
  }

  implicit class RichIdentity[T](v: => T) {
    def continue = Future.successful(v)
  }

  implicit class RichThrowableHalt(t: Throwable) {
    def haltWith(status: => StatusCode)(f: String => String = identity): Future[Unit] = {
      Future.failed(new HaltException(HttpResponse(status, f(t.getMessage))))
    }
  }
}
