package com.paypal.stingray.common

import scalaz.syntax.std.option._
import scalaz.std.list._
import scalaz.NonEmptyList
import concurrent.Future

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.option
 *
 * User: aaron
 * Date: 5/12/12
 * Time: 4:24 PM
 */

package object option {

  implicit class RichOption[T](option: Option[T]) {

    def executeIfNone(fn: => Unit): Option[T] = {
      sideEffectNone(fn)
    }

    def sideEffectNone(fn: => Unit): Option[T] = {
      option.ifNone {
        fn
      }
      option
    }

    def sideEffectSome(fn: T => Unit): Option[T] = {
      option.foreach {
        fn(_)
      }
      option
    }

    def orThrow(t: => Throwable): T = option | (throw t)

    def toFuture(t: => Throwable): Future[T] = {
      option.map(Future.successful(_)) | Future.failed(t)
    }

  }

  implicit class RichOptionTuple[T, U](optionTuple: (Option[T], Option[U])) {
    def fold[V](bothSome: (T, U) => V,
                leftSome: T => V,
                rightSome: U => V,
                bothNone: => V): V = optionTuple match {
      case (Some(t), Some(u)) => bothSome(t, u)
      case (Some(t), None) => leftSome(t)
      case (None, Some(u)) => rightSome(u)
      case (None, None) => bothNone
    }
  }

  implicit class RichOptionBoolean(optionBoolean: Option[Boolean]) {
    def orFalse: Boolean = optionBoolean | false
    def orTrue: Boolean = optionBoolean | true
  }

  implicit class RichOptionNel[A](optionNel: Option[NonEmptyList[A]]) {
    def list: List[A] = ~optionNel.map(_.list)
  }

}
