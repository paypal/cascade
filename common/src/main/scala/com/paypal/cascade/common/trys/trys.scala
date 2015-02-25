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

import scala.util.{Failure, Success, Try, Either}
import scala.concurrent.Future

/**
 * Convenience wrappers and methods for working with [[scala.util.Try]].
 * Named `trys` so that it doesn't conflict with the `try` keyword (backticks on packages are unattractive).
 */
package object trys {

  /**
   * Implicit wrapper for Try objects
   * @param self the Try object to be wrapped
   * @tparam A the success type of the Try
   */
  implicit class RichTry[A](self: => Try[A]) {

    /**
     * Converts this `Try[A]` to an [[scala.util.Either]] with a Throwable Left type
     * @return an Either based on this Try
     */
    def toEither: Either[Throwable, A] = {
      try {
        Right(self.get)
      } catch {
        case e: Throwable => Left(e)
      }
    }

    /**
     * Converts this `Try[A]` to an [[scala.util.Either]] with an arbitrary Left type
     * @param f converts from a Throwable to an arbitrary type
     * @tparam LeftT the Left type to use
     * @return an Either based on this Try
     */
    def toEither[LeftT](f: Throwable => LeftT): Either[LeftT, A] = {
      try {
        Right(self.get)
      } catch {
        case e: Throwable => Left(f(e))
      }
    }

    /**
     * Converts this `Try[A]` to a [[scala.concurrent.Future]]
     *
     * @return a Future based on this Try
     */
    def toFuture: Future[A] = {
      self match {
        case Success(s) => Future.successful(s)
        case Failure(e) => Future.failed(e)
      }
    }
  }

  /**
   * Transform Option[Try[A]] to Try[Option[A]]
   *
   * @param o Option[Try[A]] to transform
   * @tparam A particular type of Try
   * @return o Transformed to a Try[Option[A]]
   */
  def sequenceOptionTry[A](o: Option[Try[A]]): Try[Option[A]] = {
    o.map(_.map(Option.apply)).getOrElse(Success(None))
  }

  /**
   * Transform a List[Try[A]] to Try[List[A]]
   *
   * @param l List[Try[A]] to transform
   * @tparam A specific type of Try[List]
   * @return l transformed to a Try[List[A]]
   */
  def sequenceListTry[A](l: List[Try[A]]): Try[List[A]] = {
    def addTry(builder: Try[Vector[A]], next: Try[A]): Try[Vector[A]] = builder.flatMap(t => next.map(t :+ _))
    l.foldLeft(Try(Vector[A]()))(addTry).map(_.toList)
  }

}
