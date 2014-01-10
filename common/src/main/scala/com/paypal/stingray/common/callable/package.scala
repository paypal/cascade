package com.paypal.stingray.common

import java.util.concurrent.Callable
import language.implicitConversions

/**
 * Created by IntelliJ IDEA.
 *
 * com.paypal.stingray.common.callable
 *
 * User: aaron
 * Date: 8/15/12
 * Time: 2:33 PM
 */

package object callable {

  def callable[T](t: => T): Callable[T] = new Callable[T] {
    override def call(): T = t
  }

  implicit def callByNameToCallable[T](t: => T): Callable[T] = callable(t)

}
