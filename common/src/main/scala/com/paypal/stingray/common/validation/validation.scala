package com.paypal.stingray.common

import scalaz._
import Scalaz._
import language.higherKinds
import scala.concurrent.Future

/**
 * Created by IntelliJ IDEA.
 * User: taylor
 * Date: 4/10/12
 * Time: 11:40 AM
 */

package object validation {

  /**
   * the standard usage of validation to capture a throwable
   * @tparam T the success type
   */
  type ThrowableValidation[T] = Validation[Throwable, T]

  /**
   * the standard usage of ValidationNel to capture a throwable
   * @tparam T the success type
   */
  type ThrowableValidationNel[T] = ValidationNel[Throwable, T]

  /**
   * Example usage:
   *
   *   val r: Validation[Throwable, Int] = validating { "1".toInt }
   */
  def validating[A](a: => A): Validation[Throwable, A] = Validation.fromTryCatch(a)

  implicit class RichValidation[E, A](value: Validation[E, A]) {

    /**
     * Example usage:
     *
     * val newValidation: Validation[CustomException, Int] = validating {
     *   somethingThatThrowsOrReturnsInt
     * } mapFailure { t: Throwable =>
     *   new CustomException()
     * }
     */
    def mapFailure[EE](fn: E => EE): Validation[EE, A] = {
      value.leftMap(fn)
    }

    /**
     * equivalent to mapFailure
     */
    def catching[EE](fn: E => EE): Validation[EE, A] = mapFailure(fn)

    def toFuture(fn: E => Throwable): Future[A] = {
      value match {
        case Success(s) => Future.successful(s)
        case Failure(t) => Future.failed(fn(t))
      }
    }

  }

  implicit class RichValidationThrowable[E <: Throwable, A](value: Validation[E, A]) {

    /**
     * either converts the failure case or throws if failure function is not defined at the Throwable
     * in the failure case
     */
    def mapFailureOrThrow[EE](fn: PartialFunction[E, EE]): Validation[EE, A] = value match {
      case Success(s) => s.success
      case Failure(t) => if(fn.isDefinedAt(t)) {
        fn(t).fail
      } else {
        throw t
      }
    }

    /**
     * same as mapFailureOrThrow
     */
    def catchingOrThrow[EE](fn: PartialFunction[E, EE]): Validation[EE, A] = mapFailureOrThrow(fn)

    /**
     * Return the success or throw the failure
     */
    def getOrThrow: A = value match {
      case Success(s) => s
      case Failure(t) => throw t
    }

    /**
     * Return the success or throw the failure, with a chance to map the exception
     */
    def getOrThrow[EE <: Throwable](fn: PartialFunction[E, EE]): A = value match {
      case Success(s) => s
      case Failure(t) => if(fn.isDefinedAt(t)) {
        throw fn(t)
      } else {
        throw t
      }
    }

    def toFuture: Future[A] = {
      value match {
        case Success(s) => Future.successful(s)
        case Failure(t) => Future.failed(t)
      }
    }
  }

  implicit class RichValidationNel[E, A](value: Validation[NonEmptyList[E], A]) {

    /**
     * Lifts a function E => EE into the composition of the NonEmptyList
     * and Validation.FailProjection Functors. Works similar to
     * [[com.paypal.stingray.common.validation.RichValidation.mapFailure()]] except
     * it allows transformation of the exception type in a ValidationNel
     *
     * scala> nel("1", "2").fail[Int] mapFailures { new Exception(_) }
     * Failure(NonEmptyList(java.lang.Exception: 1, java.lang.Exception: 2))
     */
    def mapFailures[EE](fn: E => EE): ValidationNel[EE,A] = value match {
      case Success(a) => a.success
      case Failure(errs) => errs.map(fn).fail
    }

  }

  implicit class RichContainerOfValidations[M[_]: Traverse, E, S](container: M[Validation[E, S]]) {

    private type VNel[X] = ValidationNel[E, X]

    /**
     * @return a ValidationNel[ E, M[S] ], converted from the original M[ Validation[E, S] ].
     *         for example, this will convert a List[Validation[Throwable, MyClass]] into a ValidationNel[Throwable, List[MyClass]].
     *         if there were any failures in the original list, the resulting ValidationNel will be a Failure containing a
     *         NonEmptyList with all the Failures that were inside the original list.
     *         otherwise the resulting ValidationNel will be a Success with a List of all of the MyClass elements in the original list
     */
    lazy val ValidationNel: ValidationNel[E, M[S]] = {
      //first, convert the M[Validation[E, S]] into a M[ValidationNel[E, S]], because the sequence call (below) requires
      //that A <:< VNel (A = Validation[E, S])
      val containerValidationNel: M[ValidationNel[E, S]] = container.map { validation =>
        validation.toValidationNel
      }
      containerValidationNel.sequence[VNel, S]
    }

  }

}
