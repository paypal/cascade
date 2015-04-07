/**
 * Copyright 2013-2015 PayPal
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
package com.paypal.cascade.common.util

import scala.reflect._

/*
 * Utility classes for class casting.
 *
 * {{{
 *    import com.paypal.cascade.util.casts._
 *
 *   // `.cast[T]` casts an object to type T, returning an Option[T] which is some iff the object may be cast, none otherwise
 *   def castToString(obj: Obj): String =
 *     obj.cast[String].getOrElse(throw new ClassCastException("not a String")
 *
 *   // `.cast[T]` on an Option[A] returns an Option[T] which is Some[T] containing the casted contents of the original
 *   // option iff those contents may be cast to a T, None otherwise.
 *   def castOptionToString(opt: Option[_]): String =
 *     opt.cast[String].getOrElse(throw new ClassCastException("not a String"))
 *
 *   // `.cast[T]` on an Array[A] returns a Array[T] containing only the elements of the original array
 *   // which may be cast to a T
 *   def findStringsInArray(arr: Array[_]): Array[String] =
 *     arr.cast[String]
 *
 *   // `.cast[T]` on a Traversable[A] returns a Traversable[T] containing only the elements of the original traverable
 *   // which may be cast to a T
 *   def findStringsInTraversable(tr: Traversable[_]): Traversable[String] =
 *     tr.cast[String]
 *
 *   // `.cast[T]` on a List[A] returns a List[T] containing only the elements of the original list
 *   // which may be cast to a T
 *   def findStringsInList(li: List[_]): List[String] =
 *     li.cast[String]
 * }}}
 */
package object casts {

  /**
   * Wrapper to allow any class to be cast, based on a ClassTag for that class
   * @param any the class to be cast
   */
  implicit class CastableAny(any: Any) {

    private def getSource(a: Any): ClassTag[_] = {
      any match {
        case _: Boolean => classTag[Boolean]
        case _: Byte => classTag[Byte]
        case _: Char => classTag[Char]
        case _: Short => classTag[Short]
        case _: Int => classTag[Int]
        case _: Long => classTag[Long]
        case _: Float => classTag[Float]
        case _: Double => classTag[Double]
        case _ => ClassTag(any.getClass)
      }
    }

    /**
     * Cast the wrapped object as type `T`, optionally returning the newly cast object if the cast was successful
     * @param target the ClassTag of type `T`
     * @tparam T the type to which this wrapped object will be cast
     * @return optionally, the newly cast object, or None if the cast was unsuccessful
     */
    def cast[T](implicit target: ClassTag[T]): Option[T] = {
      Option(any).flatMap { _ =>
        val source = getSource(any)
        if (target.runtimeClass.isAssignableFrom(source.runtimeClass)) {
          Some(any.asInstanceOf[T])
        } else {
          None
        }
      }
    }

    /**
     * Cast the wrapped object as type `T` and optionally return it if it satisfies some predicate `pred`
     * @param pred the function to be satisfied, based on some value of type `T`
     * @tparam T the type to which this wrapped object will be cast
     * @return optionally, the newly cast object, or None if the cast was unsuccessful or `pred` was not satisfied
     */
    def castIf[T : ClassTag](pred: T => Boolean): Option[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }

  }

  /**
   * Wrapper to allow any Option to cast its inner object, if it exists
   * @param opt the Option whose inner object will be cast
   */
  implicit class CastableOption(opt: Option[_]) {

    /**
     * Cast the Option's inner object as type `T`, returning a new Option with the cast value if the cast
     * was successful, or None if not
     * @tparam T the type to which this wrapped Option's inner value will be cast
     * @return a new Option with the cast value, or None if the cast was unsuccessful or `opt` was None
     */
    def cast[T : ClassTag]: Option[T] = {
      opt.flatMap(_.cast[T])
    }

    /**
     * Cast the Option's inner object as type `T`, returning a new Option with the cast value if the cast
     * was successful and `pred` is then satisfied, or None if not
     * @param pred the function to be satisfied, based on some value of type `T`
     * @tparam T the type to which this wrapped Option's inner value will be cast
     * @return a new Option with the cast value, or None if the cast was unsuccessful, `pred` was not satisfied,
     *         or `opt` was None
     */
    def castIf[T : ClassTag](pred: T => Boolean): Option[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }

  }

  /**
   * Wrapper to allow a Traversable to be cast into a new Traversable with objects of type `T`. Attempts to cast
   * each individual member of the given traversable.
   * @param traversable the Traversable whose members will be cast
   */
  implicit class CastableTraversable(traversable: Traversable[_]) {

    /**
     * Cast the Traversable's objects as type `T`, returning a new Traversable with each data member that was
     * successfully cast. Has the potential to lose data, as any uncastable members are not included in the new
     * Traverable.
     * @tparam T the type to which Traversable members will be cast
     * @return a new Traversable containing all the members of this Traversable that were successfully cast
     */
    def cast[T : ClassTag]: Traversable[T] = {
      traversable.flatMap(_.cast[T])
    }

    /**
     * Cast the Traversable's objects as type `T`, returning a new Traversable with each data member that was
     * successfully cast and that satisfied `pred`. Has the potential to lose data, as any uncastable members are
     * not included in the new Traversable.
     * @param pred the function to be satisfied, based on some value of type `T`
     * @tparam T the type to which Traversable members will be cast
     * @return a new Traversable containing all the members of this Traversable that were successfully cast and
     *         that satisfied `pred`
     */
    def castIf[T : ClassTag](pred: T => Boolean): Traversable[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }

  }

  /**
   * Wrapper to allow a List to be cast into a new List with objects of type `T`. Attempts to cast each individual
   * member of the given List.
   * @param list the List whose members will be cast
   */
  implicit class CastableList(list: List[_]) {

    /**
     * Cast the List's objects as type `T`, returning a new List with each data member that was
     * successfully cast. Has the potential to lose data, as any uncastable members are not included in the new
     * List.
     * @tparam T the type to which List members will be cast
     * @return a new List containing all the members of this List that were successfully cast
     */
    def cast[T : ClassTag]: List[T] = {
      list.flatMap(_.cast[T])
    }

    /**
     * Cast the List's objects as type `T`, returning a new List with each data member that was
     * successfully cast and that satisfied `pred`. Has the potential to lose data, as any uncastable members are
     * not included in the new List.
     * @param pred the function to be satisfied, based on some value of type `T`
     * @tparam T the type to which List members will be cast
     * @return a new List containing all the members of this List that were successfully cast and
     *         that satisfied `pred`
     */
    def castIf[T : ClassTag](pred: T => Boolean): List[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }

  }

  /**
   * Wrapper to allow an Array to be cast into a new Array with objects of type `T`. Attempts to cast each individual
   * member of the given Array.
   * @param array the Array whose members will be cast
   */
  implicit class CastableArray(array: Array[_]) {

    /**
     * Cast the Array's objects as type `T`, returning a new Array with each data member that was
     * successfully cast. Has the potential to lose data, as any uncastable members are not included in the new
     * Array.
     * @tparam T the type to which Array members will be cast
     * @return a new Array containing all the members of this Array that were successfully cast
     */
    def cast[T : ClassTag]: Array[T] = {
      array.flatMap(_.cast[T])
    }

    /**
     * Cast the Array's objects as type `T`, returning a new Array with each data member that was
     * successfully cast and that satisfied `pred`. Has the potential to lose data, as any uncastable members are
     * not included in the new Array.
     * @param pred the function to be satisfied, based on some value of type `T`
     * @tparam T the type to which Array members will be cast
     * @return a new Array containing all the members of this Array that were successfully cast and
     *         that satisfied `pred`
     */
    def castIf[T : ClassTag](pred: T => Boolean): Array[T] = {
      cast[T].flatMap(c => if(pred(c)) Some(c) else None)
    }

  }

}
