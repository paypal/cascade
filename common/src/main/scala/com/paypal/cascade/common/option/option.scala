/**
 * Copyright 2013-2014 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.cascade.common

import scala.concurrent.Future

/**
 * Convenience methods and implicit wrappers for working with `scala.Option`
 */
package object option {

  /**
   * A convenience method borrowed from Scalaz. Produces an explicitly typed None.
   * @tparam T the Option type
   * @return a None of type Option[T]
   */
  def none[T]: Option[T] = None: Option[T]

  /**
   * Allows any object to be wrapped in a Some or an Option. Note that some objects provide their own
   * `toOption` or similar methods; provided methods should be preferred over these implicits, in these cases.
   *
   * {{{
   *   import com.paypal.cascade.common.option._
   *   val a = 5.some // Option[Int], equal to Some(5)
   * }}}
   *
   * @param any the wrapped object
   * @tparam T the wrapped object's type, and the type of the resulting Option
   */
  implicit class Any2Option[T](any: T) {

    /**
     * Wraps the given object in a Some.
     *
     * NOTE: calling `.some` on a null value will throw an IllegalArgumentException. Therefore, only use `.some` when
     * you absolutely know that the parameter is a constant and always will be.
     * Overall, prefer `.opt` for null safety.
     * @return the wrapped object
     */
    @throws[IllegalArgumentException]
    def some: Option[T] = {
      val wrapped = Option(any)
      if (wrapped.isEmpty) {
        throw new IllegalArgumentException("Some(null) is forbidden.")
      }
      wrapped
    }

    /**
     * Wraps the given object in an Option, which resolves to None if the given object reference is null.
     * @return optionally, the wrapped object
     */
    def opt: Option[T] = Option(any)
  }

  /**
   * Extends an Option for side effects based on the optional value.
   * @param option the wrapped Option
   * @tparam T the type of the optional value
   */
  implicit class RichOption[T](option: Option[T]) {

    /**
     * If this Option is None, throw the given Throwable; else, return the value inside the Option
     * @param t the Throwable to potentially throw
     * @return the value inside the wrapped Option
     * @throws Throwable if the Option is None
     */
    @throws[Throwable]
    def orThrow(t: => Throwable): T = option.getOrElse(throw t)

    /**
     * Map the Option into Future success/fail states: if the Option is a Some, yield a Future with its value;
     * if the Option is None, yield a Future with Throwable t as its failure state
     * @param t the Throwable to potentially wrap as a failure
     * @return the Future-wrapped value of this Option, or a Future-wrapped failure Throwable
     */
    def toFuture(t: => Throwable): Future[T] = {
      option.map(Future.successful).getOrElse(Future.failed(t))
    }
  }

  /**
   * Convenience methods for working with pairs of Options.
   *
   * {{{
   *   import com.paypal.cascade.common.option._
   *   val a = (Some("hi"), None)
   *   a.fold( ... )
   * }}}
   */
  implicit class RichOptionTuple[T, U](optionTuple: (Option[T], Option[U])) {
    /**
     * Implements fold across a pair of Options
     * @param bothSome if both Options are defined
     * @param leftSome if only the first Option is defined
     * @param rightSome if only the second Option is defined
     * @param bothNone if neither is defined
     * @tparam V the resulting type of this fold
     * @return the result of a pattern match over a pair
     */
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

  /**
   * Convenience methods for working with Option[Boolean] types
   * @param optionBoolean this Option
   *
   * {{{
   *   import com.paypal.cascade.common.option._
   *   val a = Some(true)
   *   a.orFalse  // => true, from the value in a
   *   a.orTrue   // => true still, from the value in a
   *
   *   val b = None
   *   b.orFalse  // => false, because b is not defined
   *   b.orTrue   // => true, because b is not defined
   * }}}
   */
  implicit class RichOptionBoolean(optionBoolean: Option[Boolean]) {

    /**
     * Either the value inside this option, or false
     * @return the value inside this option, or false
     */
    def orFalse: Boolean = optionBoolean.getOrElse(false)

    /**
     * Either the value inside this option, or true
     * @return the value inside this option, or true
     */
    def orTrue: Boolean = optionBoolean.getOrElse(true)
  }

  /**
   * Convenience methods for working with Option[List[A]]
   * @param optionList this Option[List[T]]
   */
  implicit class OptionList[T](optionList: Option[List[T]]) {

    /**
     * Either the return value inside this option, or Nil
     * @return the value inside this option, or Nil
     */
    def orNil: List[T] = optionList.getOrElse(Nil)
  }

  /**
   * Convenience methods for working with List[Option[A]]
   * @param listOption the List[Option[A]]
   */
  implicit class ListOption[T](listOption: List[Option[T]]) {

    /**
     * Converts a List[Option[T]] to an Option[List[T]]
     * @return Some[List[T]] if all elements are some, None otherwise.
     */
    def sequence: Option[List[T]] = {
      def addOption(builder: Option[Vector[T]], next: Option[T]): Option[Vector[T]] = builder.flatMap(t => next.map(t :+ _))
      listOption.foldLeft(Option(Vector[T]()))(addOption).map(_.toList)
    }
  }

}
