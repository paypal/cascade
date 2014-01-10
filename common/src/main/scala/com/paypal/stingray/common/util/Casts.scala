package com.paypal.stingray.common.util

import scalaz._
import Scalaz._
import casts._

  /*
   * Contains utility classes for class casting. Examples:
   *
   * import com.stackmob.util.casts._
   * import scalaz._
   * import Scalaz._
   * ...
   * // `.cast[T]` casts an object to type T, returning an Option[T] which is some iff the object may be cast, none otherwise
   * def castToString(obj: Obj): String =
   *   obj.cast[String] | (throw new ClassCastException("not a String")
   *
   * // `.cast[T]` on an Option[A] returns an Option[T] which is Some[T] containing the casted contents of the original
   * // option iff those contents may be cast to a T, None otherwise.
   * def castOptionToString(opt: Option[_]): String =
   *   opt.cast[String] | (throw new ClassCastException("not a String"))
   *
   * // `.cast[T]` on an Array[A] returns a Array[T] containing only the elements of the original array
   * // which may be cast to a T
   * def findStringsInArray(arr: Array[_]): Array[String] =
   *   arr.cast[String]
   *
   * // `.cast[T]` on a Traversable[A] returns a Traversable[T] containing only the elements of the original traverable
   * // which may be cast to a T
   * def findStringsInTraversable(tr: Traversable[_]): Traversable[String] =
   *   tr.cast[String]
   *
   * // `.cast[T]` on a List[A] returns a List[T] containing only the elements of the original list
   * // which may be cast to a T
   * def findStringsInList(li: List[_]): List[String] =
   *   li.cast[String]
   */

object casts {

  implicit class CastableAny(any: Any) {
    def cast[T](implicit target: Manifest[T]): Option[T] = {
      Option(any).flatMap { _ =>
        val source = any match {
          case _: Boolean => manifest[Boolean]
          case _: Byte => manifest[Byte]
          case _: Char => manifest[Char]
          case _: Short => manifest[Short]
          case _: Int => manifest[Int]
          case _: Long => manifest[Long]
          case _: Float => manifest[Float]
          case _: Double => manifest[Double]
          case _ => Manifest.classType(any.getClass)
        }
        (target.runtimeClass.isAssignableFrom(source.runtimeClass)).option(any.asInstanceOf[T])
      }
    }
    def castIf[T](pred: T => Boolean)(implicit m: Manifest[T]): Option[T] = {
      cast[T].flatMap(c => pred(c).option(c))
    }
  }

  implicit class CastableOption(opt: Option[_]) {
    def cast[T : Manifest]: Option[T] = {
      opt.flatMap(_.cast[T])
    }
    def castIf[T : Manifest](pred: T => Boolean): Option[T] = {
      cast[T].flatMap(c => pred(c).option(c))
    }
  }

  implicit class CastableTraversable(traversable: Traversable[_]) {
    def cast[T : Manifest]: Traversable[T] = {
      traversable.flatMap(_.cast[T])
    }
    def castIf[T : Manifest](pred: T => Boolean): Traversable[T] = {
      cast[T].flatMap(c => pred(c).option(c))
    }
  }

  implicit class CastableList(list: List[_]) {
    def cast[T : Manifest]: List[T] = {
      list.flatMap(_.cast[T])
    }
    def castIf[T : Manifest](pred: T => Boolean): List[T] = {
      cast[T].flatMap(c => pred(c).option(c))
    }
  }

  implicit class CastableArray(array: Array[_]) {
    def cast[T : Manifest]: Array[T] = {
      array.flatMap(_.cast[T])
    }
    def castIf[T : Manifest](pred: T => Boolean): Array[T] = {
      cast[T].flatMap(c => pred(c).option(c))
    }
  }

}
