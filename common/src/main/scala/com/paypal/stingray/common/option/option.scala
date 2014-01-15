package com.paypal.stingray.common

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

  def none[T] = None: Option[T]

  implicit class Any2Option[T](any: T) {

    // NOTE: calling `.some` on a null value will return `Some(null): Some[T]`
    // This behavior is equivalent to directly crafting a `Some[T](null)`
    // Overall, prefer `.opt` for null safety.
    def some: Option[T] = Some(any)

    def opt: Option[T] = Option(any)
  }

  implicit class RichOption[T](option: Option[T]) {

    def executeIfNone(fn: => Unit): Option[T] = {
      sideEffectNone(fn)
    }

    def sideEffectNone(fn: => Unit): Option[T] = {
      if(option.isEmpty) {
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

    def orThrow(t: => Throwable): T = option.getOrElse(throw t)

    def toFuture(t: => Throwable): Future[T] = {
      option.map(Future.successful(_)).getOrElse(Future.failed(t))
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
    def orFalse: Boolean = optionBoolean.getOrElse(false)
    def orTrue: Boolean = optionBoolean.getOrElse(true)
  }

}
