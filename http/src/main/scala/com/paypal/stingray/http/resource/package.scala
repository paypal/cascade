package com.paypal.stingray.http

import scalaz._
import Scalaz._
import spray.http._
import spray.http.HttpEntity._
import StatusCodes._
import scala.concurrent._
import com.paypal.stingray.common.option._
import akka.actor.Status
import com.paypal.stingray.common.future._

/**
 * Created with IntelliJ IDEA.
 * User: drapp
 * Date: 3/18/13
 * Time: 10:05 AM
 * Utility methods for turning everyday datatypes into futures possibly throwing HaltException. Methods of the form orHalt create a future.
 * Methods of the form orThrowHaltException simply throw an exception and should only be used when already in a Future
 */

package object resource {

  type NoBody = Option[String]

  type NoAuth = Unit

  def halt[T](status: StatusCode,
              entity: HttpEntity = Empty,
              headers: List[HttpHeader] = Nil) = none[T].orHaltWith(status, entity, headers)


  implicit class RichOptionHalt[A](v: Option[A]) {
    def orThrowHaltException(halt: => HttpResponse): A = {
      v.orThrow(new HaltException(halt))
    }

    def orThrowHaltException(status: => StatusCode, entity: => HttpEntity = Empty, headers: => List[HttpHeader] = Nil): A = {
      orThrowHaltException(HttpResponse(status, entity, headers))
    }

    def orHalt(halt: => HttpResponse): Future[A] = v some {
      _.continue
    } none {
      Future.failed(new HaltException(halt))
    }

    def orHaltWith(status: => StatusCode, entity: => HttpEntity = Empty, headers: => List[HttpHeader] = Nil): Future[A] = {
      orHalt(HttpResponse(status, entity, headers))
    }

    def orError(entity: => HttpEntity = Empty, headers: => List[HttpHeader] = Nil): Future[A] = {
      orHaltWith(InternalServerError, entity, headers)
    }
  }

  implicit class RichEitherThrowableHalt[A](either: Throwable \/ A) {

    def orThrowHaltExceptionWithMessage(status: StatusCode)(f: String => String = identity): A = {
      either.leftMap(e => new HaltException(HttpResponse(status, f(e.getMessage)))).valueOr(throw _)
    }

    def orThrowHaltExceptionWithErrorMessage(f: String => String = identity): A = orThrowHaltExceptionWithMessage(InternalServerError)(f)

    def orHaltWithMessage(status: StatusCode)(f: String => String = identity): Future[A] = either.fold(
      l => Future.failed(new HaltException(HttpResponse(status, f(l.getMessage)))),
      r => r.continue
    )

    def orErrorWithMessage(f: String => String = identity): Future[A] = orHaltWithMessage(InternalServerError)(f)

    // Useful for returning a failure case to the sender such that the sender receives a Future[T].
    // e.g.  sender ! someEither.orFailure
    def orFailure: Any = either.valueOr(Status.Failure)

  }

  implicit class RichValidationThrowableHalt[A](v: Validation[Throwable, A]) extends RichEitherThrowableHalt(v.disjunction)

  implicit class RichEitherHalt[T, A](either: T \/ A) {
    def orThrowHaltException(haltFn: T => HttpResponse): A = {
      either.leftMap[HaltException](haltFn.map(new HaltException(_))).valueOr(throw _)
    }

    def orErrorNow(errorFn: T => HttpEntity = { _ => Empty }): A = orThrowHaltException(e => HttpResponse(InternalServerError, errorFn(e)))

    def orHalt(haltFn: T => HttpResponse): Future[A] = {
      either.leftMap[HaltException](haltFn.map(new HaltException(_))).fold(
        l => Future.failed(l),
        r => r.continue
      )
    }

    def orError(errorFn: T => HttpEntity = { _ => Empty }): Future[A] = orHalt(e => HttpResponse(InternalServerError, errorFn(e)))
  }

  implicit class RichValidationHalt[T, A](v: Validation[T, A]) extends RichEitherHalt(v.disjunction)

  implicit class RichBooleanHalt(v: Boolean) {

    def orThrowHaltException(halt: => HttpResponse) {
      v.option(()).orThrow(new HaltException(halt))
    }

    def orThrowHaltException(status: => StatusCode, entity: => HttpEntity = Empty, headers: => List[HttpHeader] = Nil) {
      orThrowHaltException(HttpResponse(status, entity, headers))
    }

    def orHalt(halt: => HttpResponse): Future[Unit] = {
      if (v) ().continue else Future.failed(new HaltException(halt))
    }

    def orHaltWith(status: => StatusCode, entity: => HttpEntity = Empty, headers: => List[HttpHeader] = Nil): Future[Unit] = {
      orHalt(HttpResponse(status, entity, headers))
    }

    def orError(entity: => HttpEntity = Empty, headers: => List[HttpHeader] = Nil): Future[Unit] = {
      orHaltWith(InternalServerError, entity, headers)
    }
  }

  implicit class RichFuture[T](v: Future[T]) {

    def orHalt(halt: PartialFunction[Throwable, HttpResponse])(implicit ec: ExecutionContext): Future[T] = {
      v.recoverWith {
        halt andThen { resp => Future.failed(new HaltException(resp)) }
      }
    }
  }

  implicit class RichIdentity[T](v: => T) {
    def continue = Future { v }
  }

  implicit class RichThrowableHalt(t: Throwable) {
    def haltWith(status: => StatusCode)(f: String => String = identity): Future[Unit] = {
      Future.failed(new HaltException(HttpResponse(status, f(t.getMessage))))
    }
  }
}
